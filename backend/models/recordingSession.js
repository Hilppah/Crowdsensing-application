const mongoose = require("mongoose");

const recordingSessionSchema = new mongoose.Schema({
  phoneModel: String,
  startTime: Date,
  endTime: Date,
  description: String,
  frequency: Number,
  chosenMeasurement: String,
  stability: { type: Number, default: 0 }
}, { versionKey: false });

module.exports = mongoose.model("RecordingSession", recordingSessionSchema);
