const mongoose = require("mongoose");

const commonFields = {
  sessionId: {
    type: mongoose.Schema.Types.ObjectId,
    ref: "RecordingSession",
    required: true,
  },
  timestamp: { type: Date, default: Date.now },
};

const GPSSchema = new mongoose.Schema({
  ...commonFields,
  latitude: { type: Number, required: true },
  longitude: { type: Number, required: true },
   timestamp: { type: Date, required: true },
  stability: { type: Number, default: 0 },
  }, { versionKey: false });

const CompassSchema = new mongoose.Schema({
  ...commonFields,
  compassData: { type: Number, required: true },
   timestamp: { type: Date, required: true },
  stability: { type: Number, default: 0 },
}, { versionKey: false });

const ProximitySchema = new mongoose.Schema({
  ...commonFields,
  proximity: { type: Number, required: true },
   timestamp: { type: Date, required: true },
  stability: { type: Number, default: 0 },
}, { versionKey: false });

const AccelerometerSchema = new mongoose.Schema({
  ...commonFields,
  accelX: { type: Number, required: true },
  accelY: { type: Number, required: true },
  accelZ: { type: Number, required: true },
   timestamp: { type: Date, required: true },
  stability: { type: Number, default: 0 },
}, { versionKey: false });

const GyroSchema = new mongoose.Schema({
  ...commonFields,
  gyroX: { type: Number, required: true },
  gyroY: { type: Number, required: true },
  gyroZ: { type: Number, required: true },
   timestamp: { type: Date, required: true },
  stability: { type: Number, default: 0 },
}, { versionKey: false });

const GPSData = mongoose.model("GPSData", GPSSchema);
const CompassData = mongoose.model("CompassData", CompassSchema);
const ProximityData = mongoose.model("ProximityData", ProximitySchema);
const AccelerometerData = mongoose.model("AccelerometerData", AccelerometerSchema);
const GyroData = mongoose.model("GyroData", GyroSchema);

module.exports = {
  GPSData,
  CompassData,
  ProximityData,
  AccelerometerData,
  GyroData,
};
