const request = require("supertest");
const express = require("express");
const {
  GPSData,
  CompassData,
  ProximityData,
  AccelerometerData,
  GyroData,
} = require("../models/sensors");

const app = express();
app.use(express.json());

function createSensorRoute(Model, routeName) {
  app.post(`/api/sensors/${routeName}`, async (req, res) => {
    try {
      const data = new Model(req.body);
      const saved = await data.save();
      res.status(201).json(saved);
    } catch (err) {
      res.status(400).json({ error: err.message });
    }
  });
}

createSensorRoute(GPSData, "gps");
createSensorRoute(CompassData, "compass");
createSensorRoute(ProximityData, "proximity");
createSensorRoute(AccelerometerData, "accelerometer");
createSensorRoute(GyroData, "gyroscope");

describe("Sensor API Endpoints", () => {
  test("Create GPS data", async () => {
    const payload = { latitude: 37.7749, longitude: -122.4194 };
    const res = await request(app).post("/api/sensors/gps").send(payload);
    expect(res.statusCode).toBe(201);
    expect(res.body.latitude).toBe(payload.latitude);
  });

  test("Create Compass data", async () => {
    const payload = { phoneModel: "507f191e810c19729de860ea", compassData: 250 };
    const res = await request(app).post("/api/sensors/compass").send(payload);
    expect(res.statusCode).toBe(201);
    expect(res.body.compassData).toBe(payload.compassData);
  });

  test("Create Proximity data", async () => {
    const payload = { phoneModel: "507f191e810c19729de860ea", proximity: 1 };
    const res = await request(app).post("/api/sensors/proximity").send(payload);
    expect(res.statusCode).toBe(201);
    expect(res.body.proximity).toBe(payload.proximity);
  });

  test("Create Accelerometer data", async () => {
    const payload = {
      phoneModel: "507f191e810c19729de860ea",
      accelX: 0.1,
      accelY: 0.2,
      accelZ: 0.3,
    };
    const res = await request(app).post("/api/sensors/accelerometer").send(payload);
    expect(res.statusCode).toBe(201);
    expect(res.body.accelX).toBe(payload.accelX);
  });

  test("Create Gyroscope data", async () => {
    const payload = {
      phoneModel: "507f191e810c19729de860ea",
      gyroX: 1.1,
      gyroY: 1.2,
      gyroZ: 1.3,
    };
    const res = await request(app).post("/api/sensors/gyroscope").send(payload);
    expect(res.statusCode).toBe(201);
    expect(res.body.gyroX).toBe(payload.gyroX);
  });

  test("Fail to create Compass data without phoneModel", async () => {
    const payload = { compassData: 123 };
    const res = await request(app).post("/api/sensors/compass").send(payload);
    expect(res.statusCode).toBe(400);
    expect(res.body).toHaveProperty("error");
  });
});
