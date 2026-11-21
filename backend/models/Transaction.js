const mongoose = require('mongoose');

const TransactionSchema = new mongoose.Schema({
  txId: { type: String, required: true, unique: true },
  userId: { type: String, required: true },
  type: { type: String, enum: ['DEPOSIT', 'WITHDRAWAL', 'CLAIM', 'PURCHASE', 'BONUS'], required: true },
  amount: { type: Number, required: true },
  feeAmount: { type: Number, default: 0.0 },
  netAmount: { type: Number, required: true },
  currency: { type: String, default: 'USDT' },
  network: { type: String, default: 'BSC BEP20' },
  walletAddress: { type: String },
  binancePrepayId: { type: String },
  binanceTransactionId: { type: String },
  status: { type: String, enum: ['PENDING', 'PROCESSING', 'APPROVED', 'COMPLETED', 'REJECTED'], default: 'PENDING' },
  createdAt: { type: Date, default: Date.now },
  updatedAt: { type: Date, default: Date.now }
});

module.exports = mongoose.model('Transaction', TransactionSchema);
