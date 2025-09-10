const mongoose = require("mongoose");

const recordingSessionSchema = new mongoose.Schema(
  {
    phoneModel: String,
    startTime: Date,
    endTime: Date,
    description: String,
    frequency: Number,
    chosenMeasurement: String,
    stability: { type: Number, default: 0 },
    gps: [
      {
        latitude: Number,
        longitude: Number,
        timestamp: Date,
        stability: Number,
      },
    ],
    compass: [
      {
        compassData: Number,
        timestamp: Date,
        stability: Number,
      },
    ],
    accelerometer: [
      {
        accelX: Number,
        accelY: Number,
        accelZ: Number,
        timestamp: Date,
        stability: Number,
      },
    ],
    gyroscope: [
      {
        gyroX: Number,
        gyroY: Number,
        gyroZ: Number,
        timestamp: Date,
        stability: Number,
      },
    ],
    proximity: [
      {
        proximity: Number,
        timestamp: Date,
        stability: Number,
      },
    ],
  },
  { versionKey: false }
);

module.exports = mongoose.model("RecordingSession", recordingSessionSchema);
