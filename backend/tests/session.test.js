const request = require("supertest");
const express = require("express");
const sessionRoutes = require("../routes/sessions");

const {
  GPSData,
  CompassData,
  ProximityData,
  AccelerometerData,
  GyroData,
} = require("../models/sensors");
const RecordingSession = require("../models/recordingSession");

const app = express();
app.use(express.json());
app.use("/api/sessions", sessionRoutes);

describe("Recording Session API", () => {
  test("POST /api/sessions - should create session and sensor data", async () => {
    const payload = {
      phoneModel: "TestModel",
      startTime: new Date().toISOString(),
      endTime: new Date().toISOString(),
      description: "Test recording",
      gps: [{ latitude: 1.1, longitude: 2.2, timestamp: new Date().toISOString() }],
      compass: [{ compassData: 45.5, timestamp: new Date().toISOString() }],
      proximity: [{ proximity: 3, timestamp: new Date().toISOString() }],
      accelerometer: [
        { accelX: 0.1, accelY: 0.2, accelZ: 0.3, timestamp: new Date().toISOString() },
      ],
      gyroscope: [{ gyroX: 1, gyroY: 2, gyroZ: 3, timestamp: new Date().toISOString() }],
    };

    const res = await request(app).post("/api/sessions").send(payload);

    expect(res.statusCode).toBe(201);
    expect(res.body).toHaveProperty("sessionId");

    const session = await RecordingSession.findById(res.body.sessionId);
    expect(session).not.toBeNull();

    const gps = await GPSData.find({ sessionId: session._id });
    expect(gps.length).toBe(1);
    expect(gps[0].latitude).toBe(1.1);
  });

   test("POST /api/sessions - should create session with more detailed sensor data", async () => {
    const payload = {
      phoneModel: "Android A55",
      startTime: new Date().toISOString(),
      endTime: new Date(Date.now() + 60000).toISOString(),
      description: "Extended test recording",
      gps: [
        { latitude: 40.7128, longitude: -74.006, timestamp: new Date().toISOString() },
        { latitude: 40.7138, longitude: -74.007, timestamp: new Date(Date.now() + 1000).toISOString() },
      ],
      compass: [
        { compassData: 45.5, timestamp: new Date().toISOString() },
        { compassData: 90, timestamp: new Date(Date.now() + 1000).toISOString() },
      ],
      proximity: [
        { proximity: 3, timestamp: new Date().toISOString() },
        { proximity: 5, timestamp: new Date(Date.now() + 1000).toISOString() },
      ],
      accelerometer: [
        { accelX: 0.1, accelY: 0.2, accelZ: 0.3, timestamp: new Date().toISOString() },
        { accelX: 0.15, accelY: 0.25, accelZ: 0.35, timestamp: new Date(Date.now() + 1000).toISOString() },
      ],
      gyroscope: [
        { gyroX: 1, gyroY: 2, gyroZ: 3, timestamp: new Date().toISOString() },
        { gyroX: 1.1, gyroY: 2.1, gyroZ: 3.1, timestamp: new Date(Date.now() + 1000).toISOString() },
      ],
    };

    const res = await request(app).post("/api/sessions").send(payload);

    expect(res.statusCode).toBe(201);
    expect(res.body).toHaveProperty("sessionId");

    const session = await RecordingSession.findById(res.body.sessionId);
    expect(session).not.toBeNull();

    const gpsData = await GPSData.find({ sessionId: session._id });
    expect(gpsData.length).toBe(2);
    expect(gpsData[0]).toHaveProperty("latitude", 40.7128);
    expect(gpsData[1]).toHaveProperty("longitude", -74.007);
  });

    test("POST /api/sessions - should create session and validate data in database", async () => {
    const payload = {
      phoneModel: "TestModel",
      startTime: new Date().toISOString(),
      endTime: new Date().toISOString(),
      description: "Test recording",
      gps: [
        { latitude: 1.1, longitude: 2.2, timestamp: new Date().toISOString() },
        { latitude: 3.3, longitude: 4.4, timestamp: new Date().toISOString() }
      ],
      compass: [
        { compassData: 45.5, timestamp: new Date().toISOString() },
        { compassData: 90.0, timestamp: new Date().toISOString() }
      ],
      proximity: [
        { proximity: 3, timestamp: new Date().toISOString() },
        { proximity: 5, timestamp: new Date().toISOString() }
      ],
      accelerometer: [
        { accelX: 0.1, accelY: 0.2, accelZ: 0.3, timestamp: new Date().toISOString() },
        { accelX: 0.4, accelY: 0.5, accelZ: 0.6, timestamp: new Date().toISOString() }
      ],
      gyroscope: [
        { gyroX: 1, gyroY: 2, gyroZ: 3, timestamp: new Date().toISOString() },
        { gyroX: 4, gyroY: 5, gyroZ: 6, timestamp: new Date().toISOString() }
      ],
    };

    const res = await request(app).post("/api/sessions").send(payload);

    expect(res.statusCode).toBe(201);
    expect(res.body).toHaveProperty("sessionId");

    const session = await RecordingSession.findById(res.body.sessionId);
    expect(session).not.toBeNull();

    const gpsData = await GPSData.find({ sessionId: session._id });
    expect(gpsData.length).toBe(2);
    expect(gpsData[0].latitude).toBe(1.1);

    const compassData = await CompassData.find({ sessionId: session._id });
    expect(compassData.length).toBe(2);
    expect(compassData[1].compassData).toBe(90.0);

    const proximityData = await ProximityData.find({ sessionId: session._id });
    expect(proximityData.length).toBe(2);
    expect(proximityData[0].proximity).toBe(3);

    const accelData = await AccelerometerData.find({ sessionId: session._id });
    expect(accelData.length).toBe(2);
    expect(accelData[1].accelZ).toBe(0.6);

    const gyroData = await GyroData.find({ sessionId: session._id });
    expect(gyroData.length).toBe(2);
    expect(gyroData[0].gyroX).toBe(1);
  });
});
