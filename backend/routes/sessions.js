const express = require("express");
const router = express.Router();

const RecordingSession = require("../models/recordingSession");
const {
  GPSData,
  CompassData,
  ProximityData,
  AccelerometerData,
  GyroData,
  WiFiData,
  BluetoothData
} = require("../models/sensors");

router.post("/", async (req, res) => {
  try {
    console.log("Received data:", req.body);
    const {
      phoneModel,
      startTime,
      endTime,
      description,
      frequency,
      chosenMeasurement,
      stability,
      gps,
      compass,
      proximity,
      accelerometer,
      gyroscope,
      wifi,
      bluetooth
    } = req.body;

    const session = new RecordingSession({
      phoneModel,
      startTime,
      endTime,
      description,
      frequency,
      chosenMeasurement,
      stability,
      gps,
      compass,
      proximity,
      accelerometer,
      gyroscope,
      wifi,
      bluetooth
    });

    await session.save();
    console.log("Session created successfully");
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

router.get('/summary', async (req, res) => {
  try {
    const sessions = await RecordingSession.find(
      {},
      'phoneModel startTime endTime description chosenMeasurement'
    )
    .sort({ startTime: -1 })
    .limit(10);

    res.json(sessions);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

router.get("/:id", async (req, res) => {
  try {
    const session = await RecordingSession.findById(req.params.id);
    if (!session) return res.status(404).json({ error: "Session not found" });
    console.log("Data retrieved successfully");
    res.json({ session });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

router.delete("/:id", async (req, res) => {
  try {
    const session = await RecordingSession.findByIdAndDelete(req.params.id);
    if (!session) return res.status(404).json({ error: "Session not found" });
    res.json({ message: "Deleted successfully" });
    console.log("Session deleted successfully");
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: err.message });
  }
});
module.exports = router;
