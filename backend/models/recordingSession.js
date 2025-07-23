const mongoose = require("mongoose");

const recordingSessionSchema = new mongoose.Schema({
  phoneModel: { type: String, required: true },
  startTime: { type: Date, required: true },
  endTime: { type: Date, required: true },
  description: { type: String },
}, { versionKey: false });

module.exports = mongoose.model("RecordingSession", recordingSessionSchema);
