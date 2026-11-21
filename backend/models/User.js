const mongoose = require('mongoose');

const UserSchema = new mongoose.Schema({
  id: { type: String, required: true, unique: true },
  fullName: { type: String, required: true },
  email: { type: String, required: true, unique: true, lowercase: true, trim: true },
  password: { type: String, required: true },
  address: { type: String, default: '' },
  country: { type: String, default: 'Bangladesh' },
  district: { type: String, default: '' },
  city: { type: String, default: '' },
  avatarId: { type: Number, default: 1 },
  
  // Balances
  availableBalance: { type: Number, default: 0.0 },
  holdingBalance: { type: Number, default: 0.0 },
  bonusBalance: { type: Number, default: 0.0 },
  
  // Verification & Status
  isEmailVerified: { type: Boolean, default: false },
  emailOtp: { type: String, default: '' },
  otpExpiresAt: { type: Date },
  kycStatus: { type: String, enum: ['UNVERIFIED', 'PENDING', 'VERIFIED', 'REJECTED'], default: 'UNVERIFIED' },
  kycSubmittedAt: { type: Date },
  
  // Referral & Role
  referralCode: { type: String, unique: true },
  referredBy: { type: String, default: null },
  role: { type: String, enum: ['USER', 'ADMIN'], default: 'USER' },
  isBanned: { type: Boolean, default: false },
  
  createdAt: { type: Date, default: Date.now }
});

module.exports = mongoose.model('User', UserSchema);
