require("dotenv").config();
const express = require("express");
const mongoose = require("mongoose");
const {
  GPSData,
  CompassData,
  ProximityData,
  AccelerometerData,
  GyroData,
} = require("./models/sensors");

const app = express();
const port = 3000;

mongoose
  .connect(process.env.MONGO_URI)
  .then(() => console.log("Connected to MongoDB Atlas"))
  .catch((err) => console.error("MongoDB connection error:", err));

app.use(express.json());

function createSensorRoute(Model, routeName) {
  app.post(`/api/sensors/${routeName}`, async (req, res) => {
    try {
      const data = new Model(req.body);
      const saved = await data.save();
      res.status(201).json(saved);
    } catch (err) {
      console.error("Validation error:", err);
      res.status(400).json({ error: err.message });
    }
  });
}

createSensorRoute(GPSData, "gps");
createSensorRoute(CompassData, "compass");
createSensorRoute(ProximityData, "proximity");
createSensorRoute(AccelerometerData, "accelerometer");
createSensorRoute(GyroData, "gyroscope");

app.listen(port, () => {
  console.log(`Server running on http://localhost:${port}`);
});
