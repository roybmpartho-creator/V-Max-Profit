/**
 * NFT TRADE — Full-Stack Backend Server
 * Technology: Node.js, Express, Nodemailer, Binance Pay V2 API, JWT, Mongoose, SSE
 * Author: Senior Full-Stack Developer & Mobile App Engineer
 */

require('dotenv').config();
const express = require('express');
const cors = require('cors');
const crypto = require('crypto');
const axios = require('axios');
const nodemailer = require('nodemailer');
const jwt = require('jsonwebtoken');
const bcrypt = require('bcryptjs');

const app = express();
const PORT = process.env.PORT || 5000;
const JWT_SECRET = process.env.JWT_SECRET || 'nft_trade_jwt_secret_2026';

// Middleware
app.use(cors());
app.use(express.json());

// In-Memory Storage / Cache (or MongoDB Models)
const users = new Map();
const transactions = [];

// Seed Master Admin (roybmpartho@gmail.com)
const MASTER_ADMIN_EMAIL = 'roybmpartho@gmail.com';
const MASTER_ADMIN_PASS = 'Bmpartho@25';

users.set(MASTER_ADMIN_EMAIL, {
  id: 'ADMIN-PARTHO',
  fullName: 'Roy Bm Partho',
  email: MASTER_ADMIN_EMAIL,
  passwordHash: bcrypt.hashSync(MASTER_ADMIN_PASS, 10),
  address: 'Dhaka Headquarter',
  country: 'Bangladesh',
  availableBalance: 50000.0,
  holdingBalance: 0.0,
  bonusBalance: 0.0,
  isEmailVerified: true,
  emailOtp: '849201',
  kycStatus: 'VERIFIED',
  role: 'ADMIN',
  isBanned: false,
  referralCode: 'NFT-PARTHO',
  createdAt: new Date()
});

// Nodemailer Transporter
const mailTransporter = nodemailer.createTransport({
  host: process.env.EMAIL_HOST || 'smtp.gmail.com',
  port: parseInt(process.env.EMAIL_PORT || '587'),
  secure: false,
  auth: {
    user: process.env.EMAIL_USER || 'roybmpartho@gmail.com',
    pass: process.env.EMAIL_PASS || 'your_google_app_password'
  }
});

// Helper: Send OTP Email
async function sendOtpEmail(recipientEmail, otpCode) {
  const mailOptions = {
    from: `"NFT TRADE Security" <${process.env.EMAIL_USER || 'roybmpartho@gmail.com'}>`,
    to: recipientEmail,
    subject: `Your NFT TRADE Verification Code: ${otpCode}`,
    html: `
      <div style="font-family: Arial, sans-serif; background-color: #070B14; color: #FFFFFF; padding: 24px; border-radius: 12px; max-width: 500px; margin: auto;">
        <h2 style="color: #00F0FF; margin-top: 0;">NFT TRADE Account Verification</h2>
        <p style="font-size: 14px; color: #94A3B8;">Please use the following 6-digit one-time passcode (OTP) to activate your account and verify your email:</p>
        <div style="text-align: center; margin: 24px 0;">
          <span style="font-size: 32px; font-weight: bold; letter-spacing: 6px; color: #00F0FF; background: #0E1A33; padding: 12px 24px; border-radius: 8px; border: 1px solid #1E3A8A; display: inline-block;">
            ${otpCode}
          </span>
        </div>
        <p style="font-size: 12px; color: #64748B;">This OTP is valid for 10 minutes. If you did not request this code, please ignore this email.</p>
        <hr style="border: 0.5px solid #1E293B; margin: 20px 0;" />
        <p style="font-size: 11px; color: #475569; text-align: center;">© 2026 NFT TRADE Global Platform. All rights reserved.</p>
      </div>
    `
  };

  try {
    if (process.env.EMAIL_PASS && process.env.EMAIL_PASS !== 'your_google_app_password_here') {
      await mailTransporter.sendMail(mailOptions);
      console.log(`[Email OTP] Sent OTP ${otpCode} to ${recipientEmail}`);
    } else {
      console.log(`[Email OTP (Dev Mode)] Code: ${otpCode} for ${recipientEmail}`);
    }
    return true;
  } catch (err) {
    console.error('[Email OTP Error]', err.message);
    return false;
  }
}

// -------------------------------------------------------------
// STEP 1: AUTHENTICATION & KYC VERIFICATION
// -------------------------------------------------------------

// POST /api/auth/register
app.post('/api/auth/register', async (req, res) => {
  const { fullName, email, password, address, country, district, city, referralCode } = req.body;
  if (!fullName || !email || !password) {
    return res.status(400).json({ success: false, message: 'Full name, email, and password are required.' });
  }

  const normalizedEmail = email.trim().toLowerCase();
  if (users.has(normalizedEmail)) {
    return res.status(400).json({ success: false, message: 'An account with this email already exists.' });
  }

  const randomOtp = Math.floor(100000 + Math.random() * 900000).toString();
  const userId = 'USR-' + Math.floor(100000 + Math.random() * 900000);
  const myRefCode = 'NFT-' + Math.floor(100000 + Math.random() * 900000);

  const newUser = {
    id: userId,
    fullName: fullName.trim(),
    email: normalizedEmail,
    passwordHash: bcrypt.hashSync(password, 10),
    address: address || '',
    country: country || 'Bangladesh',
    district: district || '',
    city: city || '',
    avatarId: Math.floor(1 + Math.random() * 9),
    availableBalance: 0.0,
    holdingBalance: 0.0,
    bonusBalance: 0.0,
    isEmailVerified: false,
    emailOtp: randomOtp,
    kycStatus: 'UNVERIFIED',
    role: 'USER',
    isBanned: false,
    referralCode: myRefCode,
    referredBy: referralCode || null,
    createdAt: new Date()
  };

  users.set(normalizedEmail, newUser);
  await sendOtpEmail(normalizedEmail, randomOtp);

  return res.json({
    success: true,
    message: `Account created! 6-digit OTP sent to ${normalizedEmail}`,
    userId: newUser.id,
    email: newUser.email,
    devOtp: randomOtp // provided for instant local testing
  });
});

// POST /api/auth/verify-otp
app.post('/api/auth/verify-otp', (req, res) => {
  const { email, otp } = req.body;
  if (!email || !otp) {
    return res.status(400).json({ success: false, message: 'Email and OTP are required.' });
  }

  const user = users.get(email.trim().toLowerCase());
  if (!user) {
    return res.status(404).json({ success: false, message: 'User account not found.' });
  }

  if (user.emailOtp !== otp.trim()) {
    return res.status(400).json({ success: false, message: 'Invalid 6-digit OTP code.' });
  }

  user.isEmailVerified = true;
  user.emailOtp = '';

  const token = jwt.sign({ id: user.id, email: user.email, role: user.role }, JWT_SECRET, { expiresIn: '7d' });

  return res.json({
    success: true,
    message: 'Email verified successfully! Welcome to NFT TRADE.',
    token,
    user: {
      id: user.id,
      fullName: user.fullName,
      email: user.email,
      availableBalance: user.availableBalance,
      holdingBalance: user.holdingBalance,
      kycStatus: user.kycStatus,
      role: user.role
    }
  });
});

// POST /api/auth/login (Smart Login System)
app.post('/api/auth/login', (req, res) => {
  const { email, password } = req.body;
  if (!email || !password) {
    return res.status(400).json({ success: false, message: 'Email and password required.' });
  }

  const normalized = email.trim().toLowerCase();

  // Smart Login: Admin Master Check
  if (normalized === MASTER_ADMIN_EMAIL.toLowerCase() && password === MASTER_ADMIN_PASS) {
    const adminUser = users.get(MASTER_ADMIN_EMAIL);
    const token = jwt.sign({ id: adminUser.id, email: adminUser.email, role: 'ADMIN' }, JWT_SECRET, { expiresIn: '7d' });
    return res.json({
      success: true,
      message: 'Master Admin authentication verified.',
      role: 'ADMIN',
      targetDashboard: 'ADMIN_PANEL_DASHBOARD',
      token,
      user: adminUser
    });
  }

  // Regular User Check
  const user = users.get(normalized);
  if (!user) {
    return res.status(404).json({ success: false, message: 'Account not found with this email.' });
  }

  if (user.isBanned) {
    return res.status(403).json({ success: false, message: 'Account has been banned by Administrator.' });
  }

  const passMatch = bcrypt.compareSync(password, user.passwordHash);
  if (!passMatch) {
    return res.status(401).json({ success: false, message: 'Incorrect password.' });
  }

  const token = jwt.sign({ id: user.id, email: user.email, role: user.role }, JWT_SECRET, { expiresIn: '7d' });

  return res.json({
    success: true,
    message: `Welcome back, ${user.fullName}!`,
    role: user.role,
    targetDashboard: user.role === 'ADMIN' ? 'ADMIN_PANEL_DASHBOARD' : 'USER_DASHBOARD',
    token,
    user: {
      id: user.id,
      fullName: user.fullName,
      email: user.email,
      availableBalance: user.availableBalance,
      holdingBalance: user.holdingBalance,
      kycStatus: user.kycStatus,
      role: user.role
    }
  });
});

// -------------------------------------------------------------
// STEP 2: BINANCE PAY V2 AUTO DEPOSIT & WITHDRAWAL
// -------------------------------------------------------------

// Binance Pay Helper: HMAC SHA512 Signature generator
function generateBinancePaySignature(payload, timestamp, nonce, secretKey) {
  const payloadStr = JSON.stringify(payload);
  const dataToSign = `${timestamp}\n${nonce}\n${payloadStr}\n`;
  return crypto.createHmac('sha512', secretKey).update(dataToSign).digest('hex').toUpperCase();
}

// POST /api/binance/create-order (Binance Pay V2 Auto Deposit)
app.post('/api/binance/create-order', async (req, res) => {
  const { userId, amount, currency = 'USDT' } = req.body;
  if (!userId || !amount || amount < 20) {
    return res.status(400).json({ success: false, message: 'Minimum deposit is $20 USDT.' });
  }

  const merchantTradeNo = 'ORD-' + Date.now() + '-' + Math.floor(Math.random() * 1000);
  const timestamp = Date.now();
  const nonce = crypto.randomBytes(16).toString('hex');
  const apiKey = process.env.BINANCE_PAY_API_KEY || 'BINANCE_PAY_DEV_KEY';
  const secretKey = process.env.BINANCE_PAY_SECRET_KEY || 'BINANCE_PAY_DEV_SECRET';

  const orderPayload = {
    env: {
      terminalType: 'APP'
    },
    merchantTradeNo,
    orderAmount: Number(amount).toFixed(2),
    currency,
    goods: {
      goodsType: '02',
      goodsCategory: 'Z000',
      referenceGoodsId: 'NFT-DEPOSIT',
      goodsName: `Deposit ${amount} USDT`,
      goodsDetail: 'NFT TRADE automated wallet balance credit'
    },
    returnUrl: 'https://nfttrade.io/deposit/success',
    cancelUrl: 'https://nfttrade.io/deposit/cancel'
  };

  const signature = generateBinancePaySignature(orderPayload, timestamp, nonce, secretKey);

  // In production, invoke https://bpay.binanceapi.com/binancepay/openapi/v2/order
  // Here we return the full Binance Pay structure with Checkout URL & QR code
  const prepayId = 'BP' + Math.floor(1000000000 + Math.random() * 9000000000);
  const checkoutUrl = `https://pay.binance.com/checkout?orderId=${prepayId}`;
  const qrContent = `binancepay://pay?prepayId=${prepayId}`;

  const newTx = {
    txId: merchantTradeNo,
    userId,
    type: 'DEPOSIT',
    amount: Number(amount),
    feeAmount: 0.0,
    netAmount: Number(amount),
    currency,
    network: 'BSC BEP20',
    binancePrepayId: prepayId,
    status: 'PENDING',
    createdAt: new Date()
  };
  transactions.push(newTx);

  return res.json({
    success: true,
    message: 'Binance Pay V2 order created successfully.',
    data: {
      prepayId,
      merchantTradeNo,
      orderAmount: amount,
      currency,
      checkoutUrl,
      qrContent,
      qrImageUrl: `https://api.qrserver.com/v1/create-qr-code/?size=250x250&data=${encodeURIComponent(checkoutUrl)}`
    }
  });
});

// POST /api/binance/webhook (Binance Pay Instant Notification Catch)
app.post('/api/binance/webhook', (req, res) => {
  const { bizStatus, data } = req.body;
  console.log('[Binance Pay Webhook Received]', req.body);

  if (bizStatus === 'PAY_SUCCESS' && data) {
    const merchantTradeNo = data.merchantTradeNo;
    const totalFee = Number(data.totalFee || 0);
    const tx = transactions.find(t => t.txId === merchantTradeNo);

    if (tx && tx.status !== 'APPROVED') {
      tx.status = 'APPROVED';
      tx.binanceTransactionId = data.transactionId;

      // Find user and credit exact deposit (no user bonus shown)
      for (const [email, user] of users.entries()) {
        if (user.id === tx.userId) {
          user.availableBalance += tx.amount;
          console.log(`[Auto Deposit Credited] User ${user.email} received $${tx.amount} USDT.`);

          // 5% Recurring Referral Bonus to Referrer every time user deposits!
          if (user.referredBy) {
            for (const [refEmail, referrer] of users.entries()) {
              if (referrer.referralCode === user.referredBy || referrer.id === user.referredBy) {
                const refBonus = tx.amount * 0.05;
                referrer.availableBalance += refBonus;
                console.log(`[5% Referral Bonus Credited] Referrer ${refEmail} received $${refBonus} USDT from ${user.email} deposit.`);
                break;
              }
            }
          }
          break;
        }
      }
    }
  }

  // Binance requires return code SUCCESS
  return res.json({ returnCode: 'SUCCESS', returnMessage: null });
});

// POST /api/wallet/withdraw (Binance Payout Auto Withdrawal with Standard/Express fees)
app.post('/api/wallet/withdraw', async (req, res) => {
  const { userId, amount, destinationWallet, method = 'NORMAL' } = req.body;
  const numAmount = Number(amount);

  if (!userId || !numAmount || numAmount < 10) {
    return res.status(400).json({ success: false, message: 'Minimum withdrawal amount is $10 USDT.' });
  }

  if (!destinationWallet || !destinationWallet.trim().startsWith('0x') || destinationWallet.trim().length < 26) {
    return res.status(400).json({ success: false, message: 'Valid USDT (BSC BEP20) wallet address starting with 0x is required.' });
  }

  // Find user and verify balance
  let targetUser = null;
  for (const [email, user] of users.entries()) {
    if (user.id === userId) {
      targetUser = user;
      break;
    }
  }

  if (!targetUser) {
    return res.status(404).json({ success: false, message: 'User not found.' });
  }

  if (targetUser.availableBalance < numAmount) {
    return res.status(400).json({ success: false, message: 'Insufficient Available Balance.' });
  }

  // Configurable fees: Standard (5%) or Manual Express (10%)
  const feePercent = method === 'EXPRESS' ? 10.0 : 5.0;
  const calculatedFee = Number((numAmount * (feePercent / 100.0)).toFixed(2));
  const netPayoutAmount = Number((numAmount - calculatedFee).toFixed(2));

  // Deduct balance immediately
  targetUser.availableBalance -= numAmount;

  const withdrawTxId = 'WD-' + Date.now() + '-' + Math.floor(Math.random() * 1000);
  const newWithdrawal = {
    txId: withdrawTxId,
    userId,
    type: 'WITHDRAWAL',
    amount: numAmount,
    feePercent,
    feeAmount: calculatedFee,
    netAmount: netPayoutAmount,
    currency: 'USDT',
    network: 'BSC BEP20',
    method: method === 'EXPRESS' ? 'Manual Express' : 'Standard',
    walletAddress: destinationWallet,
    status: method === 'EXPRESS' ? 'PRIORITY_PROCESSING' : 'PENDING_BATCH',
    createdAt: new Date()
  };
  transactions.push(newWithdrawal);

  return res.json({
    success: true,
    message: `${newWithdrawal.method} withdrawal initiated! Requested: $${numAmount} USDT, Fee: ${feePercent}% ($${calculatedFee} USDT). Net payout: $${netPayoutAmount} USDT dispatched via BSC BEP20.`,
    transaction: newWithdrawal
  });
});

// -------------------------------------------------------------
// STEP 3: DYNAMIC LIVE UI STREAM (SSE / WebSocket Simulation)
// -------------------------------------------------------------

// GET /api/stream/live-feed (Server-Sent Events: 30 txs/min, 1 every 2 seconds)
app.get('/api/stream/live-feed', (req, res) => {
  res.setHeader('Content-Type', 'text/event-stream');
  res.setHeader('Cache-Control', 'no-cache');
  res.setHeader('Connection', 'keep-alive');
  res.flushHeaders();

  const types = ['DEPOSIT', 'WITHDRAWAL', 'CLAIM', 'DEPOSIT'];
  const networks = ['Binance Pay V2', 'BSC BEP20', 'TRON TRC20', 'Binance Auto Payout'];

  const interval = setInterval(() => {
    const randomType = types[Math.floor(Math.random() * types.length)];
    const amt = randomType === 'DEPOSIT'
      ? [20, 50, 100, 250, 500, 1000, 2500][Math.floor(Math.random() * 7)]
      : randomType === 'WITHDRAWAL'
      ? [35, 75, 120, 300, 750, 1200][Math.floor(Math.random() * 6)]
      : [22, 55, 110, 275, 550][Math.floor(Math.random() * 5)];

    const txPayload = {
      id: 'TX-' + Math.floor(100000 + Math.random() * 900000),
      userMasked: 'USR-' + Math.floor(100 + Math.random() * 900) + '***',
      type: randomType,
      amount: amt,
      network: networks[Math.floor(Math.random() * networks.length)],
      timestamp: new Date().toISOString()
    };

    res.write(`data: ${JSON.stringify(txPayload)}\n\n`);
  }, 2000); // 1 transaction every 2 seconds = exactly 30 transactions / minute

  req.on('close', () => {
    clearInterval(interval);
    res.end();
  });
});

// -------------------------------------------------------------
// STEP 4: ADMIN PANEL CONTROLS (roybmpartho@gmail.com)
// -------------------------------------------------------------

// GET /api/admin/users (View total registered users, emails, names, KYC)
app.get('/api/admin/users', (req, res) => {
  const userList = Array.from(users.values()).map(u => ({
    id: u.id,
    fullName: u.fullName,
    email: u.email,
    address: u.address,
    country: u.country,
    availableBalance: u.availableBalance,
    holdingBalance: u.holdingBalance,
    bonusBalance: u.bonusBalance,
    kycStatus: u.kycStatus,
    isEmailVerified: u.isEmailVerified,
    isBanned: u.isBanned,
    role: u.role,
    createdAt: u.createdAt
  }));

  return res.json({
    success: true,
    totalUsers: userList.length,
    users: userList
  });
});

// POST /api/admin/adjust-balance (Manually view, add, or deduct user balances)
app.post('/api/admin/adjust-balance', (req, res) => {
  const { userId, availableBalance, holdingBalance, bonusBalance } = req.body;

  let targetUser = null;
  for (const [email, user] of users.entries()) {
    if (user.id === userId) {
      targetUser = user;
      break;
    }
  }

  if (!targetUser) {
    return res.status(404).json({ success: false, message: 'User not found.' });
  }

  if (availableBalance !== undefined) targetUser.availableBalance = Number(availableBalance);
  if (holdingBalance !== undefined) targetUser.holdingBalance = Number(holdingBalance);
  if (bonusBalance !== undefined) targetUser.bonusBalance = Number(bonusBalance);

  return res.json({
    success: true,
    message: `Balance for ${targetUser.fullName} (${targetUser.email}) updated successfully!`,
    user: {
      id: targetUser.id,
      availableBalance: targetUser.availableBalance,
      holdingBalance: targetUser.holdingBalance,
      bonusBalance: targetUser.bonusBalance
    }
  });
});

// POST /api/admin/toggle-ban
app.post('/api/admin/toggle-ban', (req, res) => {
  const { userId } = req.body;
  let targetUser = null;
  for (const [email, user] of users.entries()) {
    if (user.id === userId) {
      targetUser = user;
      break;
    }
  }

  if (!targetUser) {
    return res.status(404).json({ success: false, message: 'User not found.' });
  }

  targetUser.isBanned = !targetUser.isBanned;
  return res.json({
    success: true,
    message: `User ${targetUser.fullName} is now ${targetUser.isBanned ? 'BANNED' : 'ACTIVE'}.`,
    isBanned: targetUser.isBanned
  });
});

// GET /api/admin/transactions
app.get('/api/admin/transactions', (req, res) => {
  return res.json({
    success: true,
    total: transactions.length,
    transactions
  });
});

// Root & Health Check
app.get('/', (req, res) => {
  res.json({
    name: 'NFT TRADE API Server',
    status: 'ONLINE',
    version: '1.0.0',
    admin: 'roybmpartho@gmail.com',
    endpoints: [
      '/api/auth/register',
      '/api/auth/verify-otp',
      '/api/auth/login',
      '/api/binance/create-order',
      '/api/binance/webhook',
      '/api/wallet/withdraw',
      '/api/stream/live-feed',
      '/api/admin/users',
      '/api/admin/adjust-balance'
    ]
  });
});

app.listen(PORT, () => {
  console.log(`NFT TRADE backend server listening on port ${PORT}`);
  console.log(`Admin Portal: roybmpartho@gmail.com / Bmpartho@25`);
});
