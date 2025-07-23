const express = require("express");
const router = express.Router();

const RecordingSession = require("../models/recordingSession");
const {
  GPSData,
  CompassData,
  ProximityData,
  AccelerometerData,
  GyroData,
} = require("../models/sensors");

router.post("/", async (req, res) => {
  try {
    const { phoneModel, startTime, endTime, description, gps, compass, proximity, accelerometer, gyroscope } = req.body;

    const session = new RecordingSession({ phoneModel, startTime, endTime, description });
    await session.save();

    const saveSensorData = async (Model, dataArray) => {
      if (Array.isArray(dataArray)) {
        for (const data of dataArray) {
          const doc = new Model({ ...data, phoneModel, sessionId: session._id });
          await doc.save();
        }
      }
    };

    await saveSensorData(GPSData, gps);
    await saveSensorData(CompassData, compass);
    await saveSensorData(ProximityData, proximity);
    await saveSensorData(AccelerometerData, accelerometer);
    await saveSensorData(GyroData, gyroscope);

    res.status(201).json({ sessionId: session._id });
  } catch (err) {
    console.error(err);
    res.status(400).json({ error: err.message });
  }
});

router.get("/", async (req, res) => {
  try {
    const sessions = await RecordingSession.find().sort({ startTime: -1 });
    res.json(sessions);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

router.get("/:id", async (req, res) => {
  const sessionId = req.params.id;

  try {
    const session = await RecordingSession.findById(sessionId);
    if (!session) return res.status(404).json({ error: "Session not found" });

    const [gps, compass, proximity, accelerometer, gyroscope] = await Promise.all([
      GPSData.find({ sessionId }),
      CompassData.find({ sessionId }),
      ProximityData.find({ sessionId }),
      AccelerometerData.find({ sessionId }),
      GyroData.find({ sessionId }),
    ]);

    res.json({
      session,
      sensorData: {
        gps,
        compass,
        proximity,
        accelerometer,
        gyroscope,
      },
    });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

module.exports = router;
