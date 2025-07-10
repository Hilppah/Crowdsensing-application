const mongoose = require("mongoose");

// GPS Schema
const GPSSchema = new mongoose.Schema({
  phoneModel: {
    type: mongoose.Schema.Types.ObjectId,
    ref: "Phone model",
    required: false,
  },
  latitude: { type: Number, required: true },
  longitude: { type: Number, required: true },
  timestamp: { type: Date, default: Date.now },
});

// Compass Schema
const CompassSchema = new mongoose.Schema({
  phoneModel: {
    type: mongoose.Schema.Types.ObjectId,
    ref: "Phone model",
    required: true,
  },
  compassData: { type: Number, required: true },
  timestamp: { type: Date, default: Date.now },
});

// Proximity Schema
const ProximitySchema = new mongoose.Schema({
  phoneModel: {
    type: mongoose.Schema.Types.ObjectId,
    ref: "Phone model",
    required: true,
  },
  proximity: { type: Number, required: true },
  timestamp: { type: Date, default: Date.now },
});

// Accelerometer Schema
const AccelerometerSchema = new mongoose.Schema({
  phoneModel: {
    type: mongoose.Schema.Types.ObjectId,
    ref: "Phone model",
    required: true,
  },
  accelX: { type: Number, required: true },
  accelY: { type: Number, required: true },
  accelZ: { type: Number, required: true },
  timestamp: { type: Date, default: Date.now },
});

const GyroSchema = new mongoose.Schema({
  phoneModel: {
    type: mongoose.Schema.Types.ObjectId,
    ref: "Phone model",
    required: true,
  },
  gyroX: { type: Number, required: true },
  gyroY: { type: Number, required: true },
  gyroZ: { type: Number, required: true },
  timestamp: { type: Date, default: Date.now },
});

const GPSData = mongoose.model("GPSData", GPSSchema);
const CompassData = mongoose.model("CompassData", CompassSchema);
const ProximityData = mongoose.model("ProximityData", ProximitySchema);
const AccelerometerData = mongoose.model(
  "AccelerometerData",
  AccelerometerSchema
);
const GyroData = mongoose.model("GyroData", GyroSchema);

module.exports = {
  GPSData,
  CompassData,
  ProximityData,
  AccelerometerData,
  GyroData,
};
