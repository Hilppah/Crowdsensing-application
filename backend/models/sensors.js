const mongoose = require("mongoose");

const GPSSchema = new mongoose.Schema({
  phoneModel: {
    type: mongoose.Schema.Types.ObjectId,
    ref: "Phone model",
    required: true,
  },
  latitude: {
    type: Number,
    required: true,
  },
  longitude: {
    type: Number,
    required: true,
  },
  timestamp: {
    type: Date,
    default: Date.now,
  },
});

const CompassSchema = new mongoose.Schema({
  phoneModel: {
    type: mongoose.Schema.Types.ObjectId,
    ref: "Phone model",
    required: true,
  },
  compassData: {
    type: Number,
    required: true,
  },
  timestamp: {
    type: Date,
    default: Date.now,
  },
});

const ProximitySchema = new mongoose.Schema({
  phoneModel: {
    type: mongoose.Schema.Types.ObjectId,
    ref: "Phone model",
    required: true,
  },
  timestamp: {
    type: Date,
    default: Date.now,
  },
  proximityData: {
    type: Number,
    required: true,
  },
});

const AccelerometerSchema = new mongoose.Schema({
  phoneModel: {
    type: mongoose.Schema.Types.ObjectId,
    ref: "Phone model",
    required: true,
  },
  accelerometerX: {
    type: Number,
    required: true,
  },
  accelerometerY: {
    type: Number,
    required: true,
  },
  accelerometerZ: {
    type: Number,
    required: true,
  },
  timestamp: {
    type: Date,
    default: Date.now,
  },
});
const GyroSchema = new mongoose.Schema({
  phoneModel: {
    type: mongoose.Schema.Types.ObjectId,
    ref: "Phone model",
    required: true,
  },
  timestamp: {
    type: Date,
    default: Date.now,
  },
  gyroX: {
    type: Number,
    required: true,
  },
  gyroY: {
    type: Number,
    required: true,
  },
  gyroZ: {
    type: Number,
    required: true,
  },
});

module.exports = mongoose.model("GPSData", GPSSchema);
module.exports = mongoose.model("CompassData", CompassSchema);
module.exports = mongoose.model("ProximityData", ProximitySchema);
module.exports = mongoose.model("GyroData", GyroSchema);
module.exports = mongoose.model("AccelerometerData", AccelerometerSchema);
