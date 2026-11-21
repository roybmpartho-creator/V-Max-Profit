/**
 * V-Max Profit — Live Backend Server & Web Portal
 * Deployed on Render: https://v-max-profit.onrender.com
 */

require('dotenv').config();
const fs = require('fs');
const path = require('path');
const express = require('express');
const cors = require('cors');
const crypto = require('crypto');
const axios = require('axios');
const nodemailer = require('nodemailer');
const jwt = require('jsonwebtoken');
const bcrypt = require('bcryptjs');

const app = express();
const PORT = process.env.PORT || 10000;
const JWT_SECRET = process.env.JWT_SECRET || 'vmax_profit_jwt_secret_2026';

// Official BEP20 USDT Treasury & Token Constants
const OFFICIAL_DEPOSIT_WALLET = '0xe59dca55642127ebac2fb92c1f0cffdfdccdd840';
const BSC_USDT_CONTRACT = '0x55d398326f99059ff775485246999027b3197955';
const TRANSFER_TOPIC = '0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef';
const BSC_RPC_URLS = [
  'https://bsc-dataseed.binance.org/',
  'https://bsc-dataseed1.defibit.io/',
  'https://bsc-dataseed1.ninicoin.io/'
];

// Master Admin Config (Server-Side Only — Never Exposed to Regular Users)
const MASTER_ADMIN_EMAIL = (process.env.ADMIN_EMAIL || 'roybmpartho@gmail.com').trim().toLowerCase();
const MASTER_ADMIN_PASS = process.env.ADMIN_PASSWORD || 'Bmpartho@25';

// Middleware
app.use(cors());
app.use(express.json({ limit: '2mb' }));

// Persistent File Store (Survives Restart / Sleep Cycles when disk persists)
const DATA_DIR = path.join(__dirname, 'data');
const DATA_FILE = path.join(DATA_DIR, 'store.json');
const PUBLIC_DIR = path.join(__dirname, 'public');
const APK_FILE_PATH = path.join(PUBLIC_DIR, 'V-Max-Profit.apk');

const users = new Map();
const pendingRegistrations = new Map();
const transactions = [];
const usedTxHashes = new Set();
const spinHistory = [];

function ensureDirectories() {
  if (!fs.existsSync(DATA_DIR)) fs.mkdirSync(DATA_DIR, { recursive: true });
  if (!fs.existsSync(PUBLIC_DIR)) fs.mkdirSync(PUBLIC_DIR, { recursive: true });
}

function saveStore() {
  try {
    ensureDirectories();
    const now = Date.now();
    for (const [k, v] of pendingRegistrations.entries()) {
      if (!v || v.expiresAt <= now) pendingRegistrations.delete(k);
    }
    const payload = {
      users: Array.from(users.values()),
      pendingRegistrations: Array.from(pendingRegistrations.values()),
      transactions: transactions.slice(-1000),
      usedTxHashes: Array.from(usedTxHashes),
      spinHistory: spinHistory.slice(-500)
    };
    fs.writeFileSync(DATA_FILE, JSON.stringify(payload, null, 2), 'utf8');
  } catch (err) {
    console.error('[Store Save Error]', err.message);
  }
}

function loadStore() {
  ensureDirectories();
  try {
    if (fs.existsSync(DATA_FILE)) {
      const raw = JSON.parse(fs.readFileSync(DATA_FILE, 'utf8'));
      if (Array.isArray(raw.users)) {
        for (const u of raw.users) {
          if (u && u.email) users.set(u.email.toLowerCase(), u);
        }
      }
      if (Array.isArray(raw.pendingRegistrations)) {
        const now = Date.now();
        for (const p of raw.pendingRegistrations) {
          if (p && p.email && p.expiresAt > now) {
            pendingRegistrations.set(p.email.toLowerCase(), p);
          }
        }
      }
      if (Array.isArray(raw.transactions)) {
        transactions.push(...raw.transactions);
      }
      if (Array.isArray(raw.usedTxHashes)) {
        for (const h of raw.usedTxHashes) usedTxHashes.add(String(h).toLowerCase());
      }
      if (Array.isArray(raw.spinHistory)) {
        spinHistory.push(...raw.spinHistory);
      }
    }
  } catch (err) {
    console.error('[Store Load Error]', err.message);
  }

  // Ensure Master Admin Account Exists
  if (!users.has(MASTER_ADMIN_EMAIL)) {
    users.set(MASTER_ADMIN_EMAIL, {
      id: 'ADMIN-PARTHO',
      fullName: 'Master Administrator',
      email: MASTER_ADMIN_EMAIL,
      passwordHash: bcrypt.hashSync(MASTER_ADMIN_PASS, 10),
      address: 'HQ',
      country: 'United States',
      availableBalance: 50000.0,
      holdingBalance: 0.0,
      bonusBalance: 0.0,
      tier100Spins: 0,
      tier1000Spins: 0,
      totalSpinWinnings: 0.0,
      isEmailVerified: true,
      emailOtp: '',
      kycStatus: 'VERIFIED',
      role: 'ADMIN',
      isBanned: false,
      referralCode: 'VMAX-PARTHO',
      referredBy: null,
      createdAt: new Date().toISOString()
    });
  }

  saveStore();
}

loadStore();

// Email OTP Sender (Nodemailer Gmail SMTP or Resend HTTP API)
const smtpPort = parseInt(process.env.EMAIL_PORT || '465', 10);
const emailUser = (process.env.EMAIL_USER || '').trim();
const emailPass = (process.env.EMAIL_PASS || '').replace(/\s+/g, '');

const mailTransporter = nodemailer.createTransport({
  host: process.env.EMAIL_HOST || 'smtp.gmail.com',
  port: smtpPort,
  secure: smtpPort === 465,
  auth: {
    user: emailUser,
    pass: emailPass
  },
  connectionTimeout: 15000,
  greetingTimeout: 15000,
  socketTimeout: 15000
});

async function sendOtpEmail(recipientEmail, otpCode) {
  const htmlBody = `
    <div style="font-family: Arial, sans-serif; background-color: #070B14; color: #FFFFFF; padding: 24px; border-radius: 12px; max-width: 500px; margin: auto;">
      <h2 style="color: #00F0FF; margin-top: 0;">V-Max Profit Verification</h2>
      <p style="font-size: 14px; color: #94A3B8;">Use this 6-digit verification code to complete your V-Max Profit registration:</p>
      <div style="text-align: center; margin: 24px 0;">
        <span style="font-size: 32px; font-weight: bold; letter-spacing: 6px; color: #00F0FF; background: #0E1A33; padding: 12px 24px; border-radius: 8px; border: 1px solid #1E3A8A; display: inline-block;">
          ${otpCode}
        </span>
      </div>
      <p style="font-size: 12px; color: #94A3B8;">This code expires in 10 minutes. Never share your OTP with anyone.</p>
      <p style="font-size: 11px; color: #475569; text-align: center;">© 2026 V-Max Profit Platform</p>
    </div>
  `;

  // Option 1: Gmail / SMTP via Nodemailer
  if (emailUser && emailPass && emailPass !== 'your_google_app_password_here') {
    try {
      const info = await mailTransporter.sendMail({
        from: `"V-Max Profit Security" <${emailUser}>`,
        to: recipientEmail,
        subject: `V-Max Profit Verification Code: ${otpCode}`,
        html: htmlBody
      });
      if (info && Array.isArray(info.accepted) && info.accepted.length > 0) {
        return { sent: true };
      }
      return { sent: false, reason: 'Email server did not accept the recipient Gmail address.' };
    } catch (err) {
      console.error('[SMTP Email OTP Error]', err.message);
      return { sent: false, reason: 'Failed to deliver OTP email via Gmail SMTP: ' + err.message };
    }
  }

  // Option 2: Resend HTTP API fallback (works on hosts restricting SMTP ports)
  const resendKey = (process.env.RESEND_API_KEY || '').trim();
  if (resendKey) {
    try {
      const resp = await axios.post(
        'https://api.resend.com/emails',
        {
          from: process.env.EMAIL_FROM || 'V-Max Profit <onboarding@resend.dev>',
          to: [recipientEmail],
          subject: `V-Max Profit Verification Code: ${otpCode}`,
          html: htmlBody
        },
        {
          headers: { Authorization: `Bearer ${resendKey}`, 'Content-Type': 'application/json' },
          timeout: 15000
        }
      );
      if (resp.status >= 200 && resp.status < 300) {
        return { sent: true };
      }
    } catch (err) {
      console.error('[Resend Email OTP Error]', err.message);
      return { sent: false, reason: 'Failed to deliver OTP email via Resend API.' };
    }
  }

  return {
    sent: false,
    notConfigured: true,
    reason: 'Gmail OTP service is not configured on Render yet. Set EMAIL_USER and Gmail App Password (EMAIL_PASS) in Render Environment.'
  };
}

function sanitizeUser(user) {
  return {
    id: user.id,
    fullName: user.fullName,
    email: user.email,
    country: user.country,
    availableBalance: Number((user.availableBalance || 0).toFixed(2)),
    holdingBalance: Number((user.holdingBalance || 0).toFixed(2)),
    bonusBalance: Number((user.bonusBalance || 0).toFixed(2)),
    tier100Spins: user.tier100Spins || 0,
    tier1000Spins: user.tier1000Spins || 0,
    totalSpinWinnings: Number((user.totalSpinWinnings || 0).toFixed(2)),
    isEmailVerified: Boolean(user.isEmailVerified),
    kycStatus: user.kycStatus || 'UNVERIFIED',
    role: user.role || 'USER',
    isBanned: Boolean(user.isBanned),
    referralCode: user.referralCode,
    referredBy: user.referredBy || null,
    createdAt: user.createdAt
  };
}

function findUserByIdOrEmail(userId, email) {
  if (email) {
    const byEmail = users.get(String(email).trim().toLowerCase());
    if (byEmail) return byEmail;
  }
  if (userId) {
    for (const u of users.values()) {
      if (u.id === userId) return u;
    }
  }
  return null;
}

function requireAuth(req, res, next) {
  const header = req.headers.authorization || '';
  const token = header.startsWith('Bearer ') ? header.slice(7) : null;
  if (!token) return res.status(401).json({ success: false, message: 'Authentication token required.' });
  try {
    req.auth = jwt.verify(token, JWT_SECRET);
    return next();
  } catch (_) {
    return res.status(401).json({ success: false, message: 'Session expired. Please sign in again.' });
  }
}

function requireAdmin(req, res, next) {
  return requireAuth(req, res, () => {
    if (req.auth.role !== 'ADMIN') {
      return res.status(403).json({ success: false, message: 'Administrator access required.' });
    }
    return next();
  });
}

// -------------------------------------------------------------
// REAL-TIME BSC BEP20 USDT VERIFIER (BscScan + Official BSC RPC)
// -------------------------------------------------------------
function extractTxHash(input) {
  if (!input) return '';
  const match = String(input).match(/0x[a-fA-F0-9]{64}/);
  return match ? match[0].toLowerCase() : String(input).trim().toLowerCase();
}

async function verifyBscUsdtTransaction(txHash) {
  const cleanHash = extractTxHash(txHash);
  if (!/^0x[a-f0-9]{64}$/.test(cleanHash)) {
    return { valid: false, message: 'Invalid TxID format. Enter a 66-character hash starting with 0x.' };
  }

  // Duplicate prevention BEFORE calling blockchain RPC
  if (usedTxHashes.has(cleanHash)) {
    return { valid: false, duplicate: true, message: 'TxID already used' };
  }

  // Try BscScan API first if key is provided, then public BSC RPC nodes
  const bscApiKey = process.env.BSCSCAN_API_KEY || '';
  if (bscApiKey) {
    try {
      const resp = await axios.get(
        `https://api.bscscan.com/api?module=proxy&action=eth_getTransactionReceipt&txhash=${cleanHash}&apikey=${bscApiKey}`,
        { timeout: 10000 }
      );
      const parsed = parseBscReceipt(resp.data && resp.data.result);
      if (parsed) return parsed;
    } catch (_) {
      // Fallback to official BSC RPC nodes
    }
  }

  for (const rpcUrl of BSC_RPC_URLS) {
    try {
      const resp = await axios.post(
        rpcUrl,
        { jsonrpc: '2.0', method: 'eth_getTransactionReceipt', params: [cleanHash], id: 1 },
        { timeout: 10000 }
      );
      if (resp.data && resp.data.result) {
        const parsed = parseBscReceipt(resp.data.result);
        if (parsed) return parsed;
      }
    } catch (_) {
      // Try next RPC node
    }
  }

  return {
    valid: false,
    message: 'Transaction not found or still confirming on BSC BEP20 network. Please wait 30 seconds and retry.'
  };
}

function parseBscReceipt(receipt) {
  if (!receipt) return null;
  if (String(receipt.status).toLowerCase() !== '0x1') {
    return { valid: false, message: 'Blockchain transaction status failed (not Successful).' };
  }

  const expectedWallet = OFFICIAL_DEPOSIT_WALLET.toLowerCase();
  const logs = Array.isArray(receipt.logs) ? receipt.logs : [];
  let matchedAmount = 0;

  for (const log of logs) {
    const contract = String(log.address || '').toLowerCase();
    const topics = Array.isArray(log.topics) ? log.topics : [];
    if (
      contract === BSC_USDT_CONTRACT &&
      topics.length >= 3 &&
      String(topics[0]).toLowerCase() === TRANSFER_TOPIC
    ) {
      const toAddress = '0x' + String(topics[2]).slice(-40).toLowerCase();
      if (toAddress === expectedWallet) {
        const hexVal = String(log.data || '0x0').replace(/^0x/, '') || '0';
        const rawWei = BigInt('0x' + hexVal);
        const usdtAmount = Number(rawWei) / 1e18;
        matchedAmount += usdtAmount;
      }
    }
  }

  if (matchedAmount <= 0) {
    return {
      valid: false,
      message: `Receiver address mismatch or not a BEP20 USDT transfer to ${OFFICIAL_DEPOSIT_WALLET}.`
    };
  }

  if (matchedAmount < 20.0) {
    return { valid: false, message: `Minimum deposit is $20 USDT. Detected $${matchedAmount.toFixed(2)} USDT.` };
  }

  return { valid: true, amount: Number(matchedAmount.toFixed(2)) };
}

// -------------------------------------------------------------
// HEALTH & STATUS
// -------------------------------------------------------------
app.get('/api/health', (req, res) => {
  res.json({
    status: 'ONLINE',
    app: 'V-Max Profit',
    network: 'BSC BEP20',
    timestamp: new Date().toISOString()
  });
});

// -------------------------------------------------------------
// AUTHENTICATION ENDPOINTS
// -------------------------------------------------------------
app.post('/api/auth/register', async (req, res) => {
  const { fullName, email, password, address, country, referralCode } = req.body;
  if (!fullName || !email || !password) {
    return res.status(400).json({ success: false, message: 'Full name, Gmail, and password are required.' });
  }

  const normalizedEmail = String(email).trim().toLowerCase();
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(normalizedEmail)) {
    return res.status(400).json({ success: false, message: 'Please enter a valid Gmail address.' });
  }
  if (String(password).length < 6) {
    return res.status(400).json({ success: false, message: 'Password must be at least 6 characters.' });
  }

  // Strictly one account per Gmail
  if (users.has(normalizedEmail)) {
    return res.status(409).json({
      success: false,
      message: 'An account with this Gmail already exists. Only one account per Gmail is allowed.'
    });
  }

  const randomOtp = String(crypto.randomInt(100000, 999999));
  const cleanedRef = referralCode
    ? String(referralCode).trim().split('/ref/').pop().trim().toUpperCase()
    : null;

  const sendResult = await sendOtpEmail(normalizedEmail, randomOtp);
  if (!sendResult.sent) {
    return res.status(sendResult.notConfigured ? 503 : 502).json({
      success: false,
      message: sendResult.reason || 'Failed to send OTP to your Gmail. Registration not completed.'
    });
  }

  // Hold in pendingRegistrations ONLY — permanent account is created only after valid OTP verification
  pendingRegistrations.set(normalizedEmail, {
    fullName: String(fullName).trim(),
    email: normalizedEmail,
    passwordHash: bcrypt.hashSync(String(password), 10),
    address: address || '',
    country: country || 'United States',
    referredBy: cleanedRef || null,
    otp: randomOtp,
    expiresAt: Date.now() + 10 * 60 * 1000,
    createdAt: new Date().toISOString()
  });
  saveStore();

  return res.json({
    success: true,
    message: `OTP sent successfully to ${normalizedEmail}. Check your Gmail inbox or Spam folder.`,
    email: normalizedEmail
  });
});

app.post('/api/auth/resend-otp', async (req, res) => {
  const { email } = req.body;
  if (!email) {
    return res.status(400).json({ success: false, message: 'Gmail address is required.' });
  }
  const normalizedEmail = String(email).trim().toLowerCase();
  if (users.has(normalizedEmail)) {
    return res.status(409).json({ success: false, message: 'This Gmail is already registered. Please sign in.' });
  }
  const pending = pendingRegistrations.get(normalizedEmail);
  if (!pending) {
    return res.status(404).json({ success: false, message: 'No pending registration found. Please fill out registration again.' });
  }

  const freshOtp = String(crypto.randomInt(100000, 999999));
  const sendResult = await sendOtpEmail(normalizedEmail, freshOtp);
  if (!sendResult.sent) {
    return res.status(sendResult.notConfigured ? 503 : 502).json({
      success: false,
      message: sendResult.reason || 'Unable to resend OTP email right now.'
    });
  }

  pending.otp = freshOtp;
  pending.expiresAt = Date.now() + 10 * 60 * 1000;
  saveStore();

  return res.json({
    success: true,
    message: `New OTP sent successfully to ${normalizedEmail}.`
  });
});

app.post('/api/auth/verify-otp', (req, res) => {
  const { email, otp } = req.body;
  if (!email || !otp) {
    return res.status(400).json({ success: false, message: 'Gmail and 6-digit OTP are required.' });
  }

  const normalizedEmail = String(email).trim().toLowerCase();
  if (users.has(normalizedEmail)) {
    return res.status(409).json({
      success: false,
      message: 'An account with this Gmail already exists. Only one account per Gmail is allowed.'
    });
  }

  const pending = pendingRegistrations.get(normalizedEmail);
  if (!pending) {
    return res.status(404).json({ success: false, message: 'No pending registration for this Gmail. Request a new OTP.' });
  }
  if (Date.now() > pending.expiresAt) {
    pendingRegistrations.delete(normalizedEmail);
    saveStore();
    return res.status(400).json({ success: false, message: 'OTP expired. Please register again to receive a fresh code.' });
  }
  if (pending.otp !== String(otp).trim()) {
    return res.status(400).json({ success: false, message: 'Incorrect 6-digit OTP code. Please check your Gmail and retry.' });
  }

  // Finalize verified user account ONLY now
  const newUser = {
    id: 'USR-' + crypto.randomInt(100000, 999999),
    fullName: pending.fullName,
    email: normalizedEmail,
    passwordHash: pending.passwordHash,
    address: pending.address || '',
    country: pending.country || 'United States',
    availableBalance: 0.0,
    holdingBalance: 0.0,
    bonusBalance: 0.0,
    tier100Spins: 0,
    tier1000Spins: 0,
    totalSpinWinnings: 0.0,
    isEmailVerified: true,
    emailOtp: '',
    kycStatus: 'UNVERIFIED',
    role: 'USER',
    isBanned: false,
    referralCode: 'VMAX-' + crypto.randomInt(100000, 999999),
    referredBy: pending.referredBy || null,
    createdAt: new Date().toISOString()
  };

  users.set(normalizedEmail, newUser);
  pendingRegistrations.delete(normalizedEmail);
  saveStore();

  const token = jwt.sign({ id: newUser.id, email: newUser.email, role: newUser.role }, JWT_SECRET, { expiresIn: '7d' });
  return res.json({
    success: true,
    message: 'Gmail verified & registration completed! Welcome to V-Max Profit.',
    token,
    user: sanitizeUser(newUser)
  });
});

app.post('/api/auth/login', (req, res) => {
  const { email, password } = req.body;
  if (!email || !password) {
    return res.status(400).json({ success: false, message: 'Email and password required.' });
  }

  const normalized = String(email).trim().toLowerCase();
  if (normalized === MASTER_ADMIN_EMAIL && password === MASTER_ADMIN_PASS) {
    const adminUser = users.get(MASTER_ADMIN_EMAIL);
    const token = jwt.sign({ id: adminUser.id, email: adminUser.email, role: 'ADMIN' }, JWT_SECRET, { expiresIn: '7d' });
    return res.json({
      success: true,
      message: 'Welcome back, Administrator.',
      token,
      user: sanitizeUser(adminUser)
    });
  }

  const user = users.get(normalized);
  if (!user) return res.status(404).json({ success: false, message: 'Account not found.' });
  if (user.isBanned) return res.status(403).json({ success: false, message: 'This account has been suspended.' });

  const passOk = bcrypt.compareSync(String(password), user.passwordHash);
  if (!passOk) return res.status(401).json({ success: false, message: 'Incorrect password.' });

  const token = jwt.sign({ id: user.id, email: user.email, role: user.role }, JWT_SECRET, { expiresIn: '7d' });
  return res.json({
    success: true,
    message: `Welcome back, ${user.fullName}!`,
    token,
    user: sanitizeUser(user)
  });
});

app.get('/api/me', requireAuth, (req, res) => {
  const user = findUserByIdOrEmail(req.auth.id, req.auth.email);
  if (!user) return res.status(404).json({ success: false, message: 'User not found.' });

  const myReferrals = Array.from(users.values()).filter(
    u => u.referredBy && (u.referredBy === user.referralCode || u.referredBy === user.id)
  );
  const qualifiedCount = myReferrals.filter(r =>
    transactions.some(t => t.userId === r.id && t.type === 'DEPOSIT' && t.status === 'APPROVED')
  ).length;
  const referralIncome = transactions
    .filter(t => t.userId === user.id && t.type === 'REFERRAL_BONUS')
    .reduce((acc, t) => acc + t.amount, 0);

  return res.json({
    success: true,
    user: sanitizeUser(user),
    referrals: {
      totalCount: myReferrals.length,
      qualifiedCount,
      totalIncome: Number(referralIncome.toFixed(2)),
      history: myReferrals.map(r => ({
        id: r.id,
        fullName: r.fullName,
        qualified: transactions.some(t => t.userId === r.id && t.type === 'DEPOSIT' && t.status === 'APPROVED'),
        createdAt: r.createdAt
      }))
    },
    transactions: transactions.filter(t => t.userId === user.id).slice(-50).reverse(),
    spinHistory: spinHistory.filter(s => s.userId === user.id).slice(-25).reverse()
  });
});

// -------------------------------------------------------------
// BEP20 AUTO-DEPOSIT VERIFICATION + 5% REFERRAL + LUCKY SPINS
// -------------------------------------------------------------
app.post('/api/deposit/verify-bep20', async (req, res) => {
  const { userId, email, txHash } = req.body;
  const cleanHash = extractTxHash(txHash);
  if (!cleanHash) {
    return res.status(400).json({ success: false, message: 'TxID is required.' });
  }

  // Fast Duplicate TxID Rejection Before Blockchain Call
  if (usedTxHashes.has(cleanHash)) {
    return res.status(409).json({ success: false, message: 'TxID already used' });
  }

  const user = findUserByIdOrEmail(userId, email);
  if (!user) {
    return res.status(404).json({ success: false, message: 'User account not found.' });
  }
  if (user.isBanned) {
    return res.status(403).json({ success: false, message: 'Account suspended.' });
  }

  const verification = await verifyBscUsdtTransaction(cleanHash);
  if (!verification.valid) {
    return res.status(verification.duplicate ? 409 : 400).json({
      success: false,
      message: verification.message
    });
  }

  const verifiedAmount = verification.amount;
  usedTxHashes.add(cleanHash);
  user.availableBalance = Number(((user.availableBalance || 0) + verifiedAmount).toFixed(2));

  // Grant Lucky Spins strictly on deposit tiers ($100+ -> 3 spins, $1000+ -> 6 spins)
  if (verifiedAmount >= 1000.0) {
    user.tier1000Spins = (user.tier1000Spins || 0) + 6;
  } else if (verifiedAmount >= 100.0) {
    user.tier100Spins = (user.tier100Spins || 0) + 3;
  }

  const depositTx = {
    txId: 'DEP-' + Date.now(),
    userId: user.id,
    userEmail: user.email,
    type: 'DEPOSIT',
    amount: verifiedAmount,
    netAmount: verifiedAmount,
    network: 'BSC BEP20',
    txHash: cleanHash,
    status: 'APPROVED',
    createdAt: new Date().toISOString()
  };
  transactions.push(depositTx);

  // Real Referral Commission: 5% ONLY when referred user deposits
  if (user.referredBy) {
    const referrer = Array.from(users.values()).find(
      r => r.referralCode === user.referredBy || r.id === user.referredBy
    );
    if (referrer && referrer.id !== user.id) {
      const commission = Number((verifiedAmount * 0.05).toFixed(2));
      referrer.availableBalance = Number(((referrer.availableBalance || 0) + commission).toFixed(2));
      transactions.push({
        txId: 'REF-' + Date.now(),
        userId: referrer.id,
        fromUserId: user.id,
        type: 'REFERRAL_BONUS',
        amount: commission,
        netAmount: commission,
        network: '5% Deposit Commission',
        status: 'APPROVED',
        createdAt: new Date().toISOString()
      });
    }
  }

  saveStore();
  return res.json({
    success: true,
    message: `Deposit verified! +$${verifiedAmount.toFixed(2)} USDT credited to your balance.`,
    deposit: depositTx,
    user: sanitizeUser(user)
  });
});

// -------------------------------------------------------------
// LUCKY SPIN REWARDS (NO $0.20 / $0.80, STRICT CAPS $1 / $9)
// -------------------------------------------------------------
app.post('/api/rewards/spin', requireAuth, (req, res) => {
  const user = findUserByIdOrEmail(req.auth.id, req.auth.email);
  if (!user) return res.status(404).json({ success: false, message: 'User not found.' });

  let tier = null;
  if ((user.tier1000Spins || 0) > 0) {
    tier = 'TIER_1000';
    user.tier1000Spins -= 1;
  } else if ((user.tier100Spins || 0) > 0) {
    tier = 'TIER_100';
    user.tier100Spins -= 1;
  } else {
    return res.status(400).json({
      success: false,
      message: 'Deposit $100+ USDT (3 Spins) or $1,000+ USDT (6 Spins) to unlock Lucky Spin.'
    });
  }

  // Allowed rewards only (strictly capped; $0.20 and $0.80 removed)
  const tier100Pool = [0.09, 0.50, 0.90, 1.00];
  const tier1000Pool = [0.90, 1.00, 3.00, 5.00, 9.00];
  const pool = tier === 'TIER_1000' ? tier1000Pool : tier100Pool;
  const prizeAmount = pool[Math.floor(Math.random() * pool.length)];

  user.availableBalance = Number(((user.availableBalance || 0) + prizeAmount).toFixed(2));
  user.totalSpinWinnings = Number(((user.totalSpinWinnings || 0) + prizeAmount).toFixed(2));

  const spinRecord = {
    id: 'SPIN-' + Date.now(),
    userId: user.id,
    tier,
    prizeAmount,
    createdAt: new Date().toISOString()
  };
  spinHistory.push(spinRecord);
  transactions.push({
    txId: spinRecord.id,
    userId: user.id,
    type: 'SPIN_REWARD',
    amount: prizeAmount,
    netAmount: prizeAmount,
    network: 'Lucky Spin Lottery',
    status: 'APPROVED',
    createdAt: new Date().toISOString()
  });

  saveStore();
  return res.json({
    success: true,
    prizeAmount,
    message: `Congratulations! You won $${prizeAmount.toFixed(2)} USDT!`,
    user: sanitizeUser(user)
  });
});

// -------------------------------------------------------------
// WITHDRAWAL ENDPOINT (BLANK WALLET REQUIRED, ADMIN APPROVAL)
// -------------------------------------------------------------
app.post('/api/wallet/withdraw', (req, res) => {
  const { userId, email, amount, destinationWallet, method = 'STANDARD' } = req.body;
  const numAmount = Number(amount);

  if (!numAmount || numAmount < 10) {
    return res.status(400).json({ success: false, message: 'Minimum withdrawal is $10 USDT.' });
  }
  const wallet = String(destinationWallet || '').trim();
  if (!wallet.startsWith('0x') || wallet.length < 26) {
    return res.status(400).json({ success: false, message: 'Enter your personal BEP20 USDT wallet address starting with 0x.' });
  }

  const user = findUserByIdOrEmail(userId, email);
  if (!user) return res.status(404).json({ success: false, message: 'User account not found.' });
  if (user.availableBalance < numAmount) {
    return res.status(400).json({ success: false, message: 'Insufficient Available Balance.' });
  }

  const isManualFree = String(method).toUpperCase().includes('MANUAL');
  const feePercent = isManualFree ? 0.0 : 10.0;
  const feeAmount = Number((numAmount * (feePercent / 100.0)).toFixed(2));
  const netAmount = Number((numAmount - feeAmount).toFixed(2));

  user.availableBalance = Number((user.availableBalance - numAmount).toFixed(2));

  const wdTx = {
    txId: 'WD-' + Date.now(),
    userId: user.id,
    userEmail: user.email,
    type: 'WITHDRAWAL',
    amount: numAmount,
    feePercent,
    feeAmount,
    netAmount,
    network: 'BSC BEP20',
    method: isManualFree ? 'Manual Payout (0% Free, 1-2 Days)' : 'Standard Fast Payout (10% Fee)',
    walletAddress: wallet,
    status: 'PENDING',
    createdAt: new Date().toISOString()
  };
  transactions.push(wdTx);
  saveStore();

  return res.json({
    success: true,
    message: `Withdrawal request submitted ($${numAmount.toFixed(2)} USDT, Net: $${netAmount.toFixed(2)} USDT). Status: Pending Admin Approval.`,
    withdrawal: wdTx,
    user: sanitizeUser(user)
  });
});

// -------------------------------------------------------------
// ADMIN CONTROLS (STRICTLY PROTECTED WITH ADMIN JWT)
// -------------------------------------------------------------
app.get('/api/admin/overview', requireAdmin, (req, res) => {
  const userList = Array.from(users.values()).map(sanitizeUser);
  const deposits = transactions.filter(t => t.type === 'DEPOSIT').reverse();
  const withdrawals = transactions.filter(t => t.type === 'WITHDRAWAL').reverse();
  return res.json({
    success: true,
    users: userList,
    deposits,
    withdrawals
  });
});

app.post('/api/admin/adjust-balance', requireAdmin, (req, res) => {
  const { userId, availableBalance, holdingBalance, bonusBalance } = req.body;
  const targetUser = findUserByIdOrEmail(userId, null);
  if (!targetUser) return res.status(404).json({ success: false, message: 'User not found.' });

  if (availableBalance !== undefined) targetUser.availableBalance = Number(availableBalance);
  if (holdingBalance !== undefined) targetUser.holdingBalance = Number(holdingBalance);
  if (bonusBalance !== undefined) targetUser.bonusBalance = Number(bonusBalance);
  saveStore();

  return res.json({
    success: true,
    message: `Balance updated for ${targetUser.id}.`,
    user: sanitizeUser(targetUser)
  });
});

app.post('/api/admin/toggle-ban', requireAdmin, (req, res) => {
  const { userId } = req.body;
  const targetUser = findUserByIdOrEmail(userId, null);
  if (!targetUser) return res.status(404).json({ success: false, message: 'User not found.' });
  if (targetUser.role === 'ADMIN') {
    return res.status(400).json({ success: false, message: 'Cannot ban administrator account.' });
  }
  targetUser.isBanned = !targetUser.isBanned;
  saveStore();

  return res.json({
    success: true,
    message: `User ${targetUser.id} is now ${targetUser.isBanned ? 'BANNED' : 'ACTIVE'}.`,
    isBanned: targetUser.isBanned
  });
});

app.post('/api/admin/withdraw/status', requireAdmin, (req, res) => {
  const { txId, status } = req.body;
  const wd = transactions.find(t => t.txId === txId && t.type === 'WITHDRAWAL');
  if (!wd) return res.status(404).json({ success: false, message: 'Withdrawal record not found.' });

  if (wd.status === 'PENDING' && status === 'REJECTED') {
    const user = findUserByIdOrEmail(wd.userId, null);
    if (user) {
      user.availableBalance = Number(((user.availableBalance || 0) + wd.amount).toFixed(2));
    }
  }
  wd.status = status === 'APPROVED' ? 'APPROVED' : 'REJECTED';
  saveStore();

  return res.json({ success: true, message: `Withdrawal ${wd.txId} updated to ${wd.status}.`, withdrawal: wd });
});

// -------------------------------------------------------------
// APK DOWNLOAD ENDPOINT (V-Max-Profit.apk)
// -------------------------------------------------------------
app.get('/download/V-Max-Profit.apk', (req, res) => {
  if (fs.existsSync(APK_FILE_PATH)) {
    res.setHeader('Content-Type', 'application/vnd.android.package-archive');
    res.setHeader('Content-Disposition', 'attachment; filename="V-Max-Profit.apk"');
    return res.sendFile(APK_FILE_PATH);
  }
  return res.status(404).json({
    success: false,
    message: 'APK package is not uploaded to backend/public/V-Max-Profit.apk yet.'
  });
});

// -------------------------------------------------------------
// V-MAX PROFIT WEB PORTAL (GET /)
// -------------------------------------------------------------
app.get('/', (req, res) => {
  const hasApk = fs.existsSync(APK_FILE_PATH);
  res.setHeader('Content-Type', 'text/html; charset=utf-8');
  res.send(`<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>V-Max Profit</title>
  <style>
    :root {
      --bg: #070B14;
      --card: #0E172A;
      --border: #1E293B;
      --cyan: #00F0FF;
      --gold: #F5B700;
      --text: #F8FAFC;
      --muted: #94A3B8;
      --green: #10B981;
      --red: #EF4444;
    }
    * { box-sizing: border-box; }
    body {
      margin: 0;
      font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
      background: var(--bg);
      color: var(--text);
      padding-bottom: 40px;
    }
    .container { max-width: 520px; margin: 0 auto; padding: 16px; }
    .topbar {
      display: flex; align-items: center; justify-content: space-between;
      padding: 14px 16px; background: var(--card); border-bottom: 1px solid var(--border);
      position: sticky; top: 0; z-index: 20;
    }
    .brand { font-size: 20px; font-weight: 800; color: var(--cyan); letter-spacing: 0.4px; }
    .brand span { color: var(--gold); }
    .card {
      background: var(--card); border: 1px solid var(--border);
      border-radius: 16px; padding: 18px; margin-top: 14px;
    }
    .apk-banner {
      background: linear-gradient(135deg, #0E233D, #091324);
      border: 1px solid rgba(0,240,255,0.4);
      border-radius: 16px; padding: 16px; margin-top: 14px;
      display: flex; align-items: center; justify-content: space-between; gap: 12px;
    }
    .btn {
      display: inline-block; padding: 11px 16px; border-radius: 12px; border: none;
      font-weight: 700; font-size: 14px; cursor: pointer; text-decoration: none; text-align: center;
    }
    .btn-cyan { background: var(--cyan); color: #04101A; }
    .btn-gold { background: var(--gold); color: #1A1200; }
    .btn-outline { background: transparent; color: var(--cyan); border: 1px solid var(--cyan); }
    .btn-block { width: 100%; display: block; margin-top: 10px; }
    input, select {
      width: 100%; padding: 12px; border-radius: 10px;
      border: 1px solid var(--border); background: #070C18; color: var(--text);
      margin-top: 8px; font-size: 14px;
    }
    label { font-size: 12px; color: var(--muted); display: block; margin-top: 10px; }
    .tabs { display: flex; gap: 8px; overflow-x: auto; margin-top: 14px; padding-bottom: 4px; }
    .tab {
      padding: 8px 14px; border-radius: 999px; background: var(--card);
      border: 1px solid var(--border); color: var(--muted); font-size: 13px;
      font-weight: 600; cursor: pointer; white-space: nowrap;
    }
    .tab.active { background: rgba(0,240,255,0.14); color: var(--cyan); border-color: var(--cyan); }
    .stat-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 10px; margin-top: 10px; }
    .stat-box { background: #080E1C; border: 1px solid var(--border); border-radius: 12px; padding: 12px; }
    .muted { color: var(--muted); font-size: 13px; }
    .toast {
      margin-top: 12px; padding: 10px 14px; border-radius: 10px; font-size: 13px;
      background: #0F2922; border: 1px solid var(--green); color: #A7F3D0; display: none;
    }
    .toast.err { background: #2D1115; border-color: var(--red); color: #FCA5A5; }
    .hidden { display: none !important; }
    .wheel-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 8px; margin-top: 12px; }
    .slot {
      background: #081124; border: 1px solid var(--border); border-radius: 10px;
      padding: 10px 4px; text-align: center; font-weight: 700; font-size: 13px; color: var(--gold);
    }
    .slot.lit { border-color: var(--cyan); background: rgba(0,240,255,0.18); color: #FFF; }
  </style>
</head>
<body>
  <div class="topbar">
    <div class="brand">V-Max <span>Profit</span></div>
    <div id="userChip" class="muted">LIVE</div>
  </div>

  <div class="container">
    <div class="apk-banner">
      <div>
        <div style="font-weight: 800; font-size: 15px;">V-Max Profit Android App</div>
        <div class="muted" style="font-size: 12px;">Official BEP20 Auto Deposit &amp; Share Engine</div>
      </div>
      ${hasApk
        ? '<a class="btn btn-cyan" href="/download/V-Max-Profit.apk">Download APK</a>'
        : '<span class="muted" style="font-size:12px;">APK Building</span>'}
    </div>

    <div id="toast" class="toast"></div>

    <!-- Auth Panel -->
    <div id="authPanel" class="card">
      <div style="display:flex; gap:8px; margin-bottom:10px;">
        <button class="btn btn-cyan" style="flex:1" onclick="showAuthMode('login')">Sign In</button>
        <button class="btn btn-outline" style="flex:1" onclick="showAuthMode('register')">Register</button>
      </div>

      <div id="loginBox">
        <label>Email</label>
        <input id="loginEmail" type="email" placeholder="you@example.com" />
        <label>Password</label>
        <input id="loginPass" type="password" placeholder="Enter password" />
        <button class="btn btn-cyan btn-block" onclick="doLogin()">Sign In to V-Max Profit</button>
      </div>

      <div id="registerBox" class="hidden">
        <label>Full Name</label>
        <input id="regName" type="text" placeholder="Your full name" />
        <label>Email</label>
        <input id="regEmail" type="email" placeholder="you@example.com" />
        <label>Password (min 6 chars)</label>
        <input id="regPass" type="password" placeholder="Create password" />
        <label>Country</label>
        <input id="regCountry" type="text" value="United States" />
        <label>Referral Code (Optional)</label>
        <input id="regRef" type="text" placeholder="VMAX-XXXXXX" />
        <button class="btn btn-cyan btn-block" onclick="doRegister()">Create V-Max Profit Account</button>
      </div>

      <div id="otpBox" class="hidden">
        <label>Enter 6-Digit Verification OTP Sent to Gmail</label>
        <input id="otpInput" type="text" maxlength="6" placeholder="Enter 6-digit OTP from Gmail" />
        <button class="btn btn-gold btn-block" onclick="doVerifyOtp()">Verify Gmail &amp; Register</button>
        <button class="btn btn-outline btn-block" onclick="doResendOtp()">Resend OTP</button>
      </div>
    </div>

    <!-- Main Dashboard -->
    <div id="appPanel" class="hidden">
      <div class="tabs">
        <button class="tab active" onclick="openTab('homeTab', this)">Home</button>
        <button class="tab" onclick="openTab('depositTab', this)">Deposit</button>
        <button class="tab" onclick="openTab('withdrawTab', this)">Withdraw</button>
        <button class="tab" onclick="openTab('spinTab', this)">Lucky Spin</button>
        <button class="tab" onclick="openTab('refTab', this)">Referral</button>
        <button id="adminTabBtn" class="tab hidden" onclick="openTab('adminTab', this)">Admin</button>
      </div>

      <div id="homeTab" class="card">
        <div class="muted">Account UID: <strong id="uidLabel" style="color:#FFF;">--</strong></div>
        <div style="font-size: 32px; font-weight: 800; color: var(--cyan); margin-top: 6px;">
          $<span id="availBal">0.00</span> <span style="font-size:14px; color:var(--muted);">USDT</span>
        </div>
        <div class="stat-grid">
          <div class="stat-box">
            <div class="muted">Top Country Today</div>
            <div id="topCountry" style="font-weight: 700; color: var(--gold); margin-top: 4px;">--</div>
          </div>
          <div class="stat-box">
            <div class="muted">Payout Hours</div>
            <div style="font-weight: 600; font-size: 13px; margin-top: 4px;">USA Time 9:00 AM - 6:00 PM</div>
          </div>
        </div>
        <button class="btn btn-outline btn-block" onclick="logout()">Sign Out</button>
      </div>

      <div id="depositTab" class="card hidden">
        <h3 style="margin-top:0;">BEP20 USDT Auto Deposit</h3>
        <label>1. Select Deposit Amount</label>
        <select id="depSelect" onchange="revealDepositAddress()">
          <option value="">-- Choose Amount --</option>
          <option value="20">$20 USDT</option>
          <option value="50">$50 USDT</option>
          <option value="100">$100 USDT (+3 Lucky Spins)</option>
          <option value="500">$500 USDT (+3 Lucky Spins)</option>
          <option value="1000">$1,000 USDT (+6 Lucky Spins)</option>
          <option value="5000">$5,000 USDT (+6 Lucky Spins)</option>
        </select>

        <div id="depPaymentBox" class="hidden" style="margin-top:14px;">
          <div style="text-align:center;">
            <img src="https://api.qrserver.com/v1/create-qr-code/?size=180x180&data=${OFFICIAL_DEPOSIT_WALLET}" alt="BEP20 QR" style="border-radius:12px; background:#FFF; padding:6px;" />
          </div>
          <label>Official BNB Smart Chain (BEP20) Wallet Address</label>
          <input type="text" readonly value="${OFFICIAL_DEPOSIT_WALLET}" onclick="this.select();navigator.clipboard?.writeText(this.value);" />
          <label>2. Enter Blockchain TxID (Transaction Hash)</label>
          <input id="depTxInput" type="text" placeholder="0x..." />
          <button class="btn btn-cyan btn-block" onclick="submitDeposit()">Verify TxID &amp; Credit Instantly</button>
        </div>
      </div>

      <div id="withdrawTab" class="card hidden">
        <h3 style="margin-top:0;">Withdraw USDT (BSC BEP20)</h3>
        <label>Your Personal BEP20 Wallet Address</label>
        <input id="wdWallet" type="text" placeholder="Paste your 0x... BEP20 address" autocomplete="off" />
        <label>Amount (Min $10 USDT)</label>
        <input id="wdAmount" type="number" min="10" placeholder="50" />
        <label>Payout Method</label>
        <select id="wdMethod">
          <option value="STANDARD">Standard Fast Payout (10% Fee)</option>
          <option value="MANUAL_FREE">Manual Payout (0% Free - 1-2 Days)</option>
        </select>
        <button class="btn btn-gold btn-block" onclick="submitWithdraw()">Submit Withdrawal Request</button>
      </div>

      <div id="spinTab" class="card hidden">
        <h3 style="margin-top:0;">Lucky Spin Lottery Rewards</h3>
        <div class="muted">Spins Remaining: <strong id="spinsLeft" style="color:var(--cyan);">0</strong></div>
        <div class="wheel-grid" id="wheelGrid">
          <div class="slot">$999999</div>
          <div class="slot">$99999</div>
          <div class="slot">$9999</div>
          <div class="slot">$999</div>
          <div class="slot">$99</div>
          <div class="slot">$9</div>
          <div class="slot">$1</div>
          <div class="slot">$0.09</div>
        </div>
        <button class="btn btn-gold btn-block" onclick="playSpin()">SPIN LOTTERY NOW</button>
      </div>

      <div id="refTab" class="card hidden">
        <h3 style="margin-top:0;">5% Referral Commission</h3>
        <div class="muted">Earn 5% instant commission whenever your invited member makes a verified deposit.</div>
        <label>Your Referral Code</label>
        <input id="myRefCode" type="text" readonly />
        <div class="stat-grid">
          <div class="stat-box"><div class="muted">Invited</div><div id="refTotal" style="font-weight:700;">0</div></div>
          <div class="stat-box"><div class="muted">Earned Bonus</div><div id="refEarned" style="font-weight:700; color:var(--green);">$0.00</div></div>
        </div>
      </div>

      <div id="adminTab" class="card hidden">
        <h3 style="margin-top:0;">Admin Management</h3>
        <label>Search User by UID or Email</label>
        <input id="adminSearch" type="text" placeholder="USR-... or email" oninput="renderAdminUsers()" />
        <div id="adminUsersList" style="margin-top:10px;"></div>
        <h4 style="margin-top:16px;">Pending Withdrawals</h4>
        <div id="adminWdList"></div>
      </div>
    </div>
  </div>

  <script>
    let token = localStorage.getItem('vmax_token') || '';
    let currentUser = null;
    let pendingEmail = '';
    let adminCache = { users: [], withdrawals: [] };

    const countries = ['Canada', 'United States', 'Bangladesh', 'United Kingdom', 'Germany'];
    const dayIndex = Math.floor(Date.now() / (24 * 60 * 60 * 1000)) % countries.length;
    document.getElementById('topCountry').textContent = countries[dayIndex];

    function showToast(msg, isErr) {
      const el = document.getElementById('toast');
      el.textContent = msg;
      el.className = isErr ? 'toast err' : 'toast';
      el.style.display = 'block';
      setTimeout(() => { el.style.display = 'none'; }, 5500);
    }

    function showAuthMode(mode) {
      document.getElementById('loginBox').classList.toggle('hidden', mode !== 'login');
      document.getElementById('registerBox').classList.toggle('hidden', mode !== 'register');
      document.getElementById('otpBox').classList.add('hidden');
    }

    function openTab(tabId, btn) {
      ['homeTab','depositTab','withdrawTab','spinTab','refTab','adminTab'].forEach(id => {
        document.getElementById(id).classList.add('hidden');
      });
      document.getElementById(tabId).classList.remove('hidden');
      document.querySelectorAll('.tab').forEach(t => t.classList.remove('active'));
      if (btn) btn.classList.add('active');
      if (tabId === 'adminTab') loadAdminOverview();
    }

    function revealDepositAddress() {
      const val = document.getElementById('depSelect').value;
      document.getElementById('depPaymentBox').classList.toggle('hidden', !val);
    }

    async function api(path, method, body) {
      const headers = { 'Content-Type': 'application/json' };
      if (token) headers['Authorization'] = 'Bearer ' + token;
      const res = await fetch(path, {
        method: method || 'GET',
        headers,
        body: body ? JSON.stringify(body) : undefined
      });
      const data = await res.json();
      if (!res.ok) throw new Error(data.message || 'Request failed');
      return data;
    }

    async function doLogin() {
      try {
        const data = await api('/api/auth/login', 'POST', {
          email: document.getElementById('loginEmail').value,
          password: document.getElementById('loginPass').value
        });
        token = data.token;
        localStorage.setItem('vmax_token', token);
        showToast(data.message);
        await refreshProfile();
      } catch (e) { showToast(e.message, true); }
    }

    async function doRegister() {
      try {
        const data = await api('/api/auth/register', 'POST', {
          fullName: document.getElementById('regName').value,
          email: document.getElementById('regEmail').value,
          password: document.getElementById('regPass').value,
          country: document.getElementById('regCountry').value,
          referralCode: document.getElementById('regRef').value
        });
        pendingEmail = data.email;
        document.getElementById('registerBox').classList.add('hidden');
        document.getElementById('otpBox').classList.remove('hidden');
        showToast(data.message || ('OTP sent successfully to ' + pendingEmail));
      } catch (e) { showToast(e.message, true); }
    }

    async function doResendOtp() {
      try {
        const data = await api('/api/auth/resend-otp', 'POST', { email: pendingEmail });
        showToast(data.message);
      } catch (e) { showToast(e.message, true); }
    }

    async function doVerifyOtp() {
      try {
        const data = await api('/api/auth/verify-otp', 'POST', {
          email: pendingEmail,
          otp: document.getElementById('otpInput').value
        });
        token = data.token;
        localStorage.setItem('vmax_token', token);
        showToast(data.message);
        await refreshProfile();
      } catch (e) { showToast(e.message, true); }
    }

    async function refreshProfile() {
      if (!token) return;
      try {
        const data = await api('/api/me');
        currentUser = data.user;
        document.getElementById('authPanel').classList.add('hidden');
        document.getElementById('appPanel').classList.remove('hidden');
        document.getElementById('userChip').textContent = currentUser.id;
        document.getElementById('uidLabel').textContent = currentUser.id;
        document.getElementById('availBal').textContent = Number(currentUser.availableBalance).toFixed(2);
        document.getElementById('spinsLeft').textContent = (currentUser.tier100Spins || 0) + (currentUser.tier1000Spins || 0);
        document.getElementById('myRefCode').value = currentUser.referralCode;
        document.getElementById('refTotal').textContent = data.referrals.qualifiedCount + ' / ' + data.referrals.totalCount;
        document.getElementById('refEarned').textContent = '$' + Number(data.referrals.totalIncome).toFixed(2);
        document.getElementById('adminTabBtn').classList.toggle('hidden', currentUser.role !== 'ADMIN');
      } catch (_) {
        logout();
      }
    }

    async function submitDeposit() {
      try {
        const data = await api('/api/deposit/verify-bep20', 'POST', {
          userId: currentUser.id,
          email: currentUser.email,
          txHash: document.getElementById('depTxInput').value
        });
        document.getElementById('depTxInput').value = '';
        showToast(data.message);
        await refreshProfile();
      } catch (e) { showToast(e.message, true); }
    }

    async function submitWithdraw() {
      try {
        const data = await api('/api/wallet/withdraw', 'POST', {
          userId: currentUser.id,
          email: currentUser.email,
          amount: Number(document.getElementById('wdAmount').value),
          destinationWallet: document.getElementById('wdWallet').value,
          method: document.getElementById('wdMethod').value
        });
        document.getElementById('wdWallet').value = '';
        document.getElementById('wdAmount').value = '';
        showToast(data.message);
        await refreshProfile();
      } catch (e) { showToast(e.message, true); }
    }

    async function playSpin() {
      const slots = document.querySelectorAll('#wheelGrid .slot');
      let step = 0;
      const timer = setInterval(() => {
        slots.forEach(s => s.classList.remove('lit'));
        slots[step % slots.length].classList.add('lit');
        step++;
      }, 90);
      try {
        const data = await api('/api/rewards/spin', 'POST', {});
        setTimeout(async () => {
          clearInterval(timer);
          slots.forEach(s => s.classList.remove('lit'));
          showToast(data.message);
          await refreshProfile();
        }, 1200);
      } catch (e) {
        clearInterval(timer);
        slots.forEach(s => s.classList.remove('lit'));
        showToast(e.message, true);
      }
    }

    async function loadAdminOverview() {
      try {
        adminCache = await api('/api/admin/overview');
        renderAdminUsers();
        const wdEl = document.getElementById('adminWdList');
        wdEl.innerHTML = adminCache.withdrawals.map(w =>
          '<div class="stat-box" style="margin-top:8px;">' +
          '<div><strong>' + w.userId + '</strong> | $' + w.amount + ' (' + w.status + ')</div>' +
          '<div class="muted" style="word-break:break-all;">Wallet: ' + w.walletAddress + '</div>' +
          (w.status === 'PENDING'
            ? '<div style="display:flex;gap:8px;margin-top:6px;">' +
              '<button class="btn btn-cyan" onclick="setWd(\\'' + w.txId + '\\',\\'APPROVED\\')">Approve</button>' +
              '<button class="btn btn-outline" onclick="setWd(\\'' + w.txId + '\\',\\'REJECTED\\')">Reject</button></div>'
            : '') +
          '</div>'
        ).join('') || '<div class="muted">No withdrawals yet.</div>';
      } catch (e) { showToast(e.message, true); }
    }

    function renderAdminUsers() {
      const q = (document.getElementById('adminSearch').value || '').toLowerCase();
      const filtered = adminCache.users.filter(u =>
        u.id.toLowerCase().includes(q) || u.email.toLowerCase().includes(q) || u.fullName.toLowerCase().includes(q)
      );
      document.getElementById('adminUsersList').innerHTML = filtered.map(u =>
        '<div class="stat-box" style="margin-top:8px;">' +
        '<div><strong>' + u.id + '</strong> — ' + u.fullName + ' (' + (u.isBanned ? 'BANNED' : 'ACTIVE') + ')</div>' +
        '<div class="muted">' + u.email + ' | Bal: $' + Number(u.availableBalance).toFixed(2) + '</div>' +
        '<button class="btn btn-outline" style="margin-top:6px;" onclick="toggleBan(\\'' + u.id + '\\')">' +
        (u.isBanned ? 'Unban' : 'Ban') + '</button></div>'
      ).join('');
    }

    async function toggleBan(userId) {
      try {
        const d = await api('/api/admin/toggle-ban', 'POST', { userId });
        showToast(d.message);
        await loadAdminOverview();
      } catch (e) { showToast(e.message, true); }
    }

    async function setWd(txId, status) {
      try {
        const d = await api('/api/admin/withdraw/status', 'POST', { txId, status });
        showToast(d.message);
        await loadAdminOverview();
      } catch (e) { showToast(e.message, true); }
    }

    function logout() {
      token = '';
      currentUser = null;
      localStorage.removeItem('vmax_token');
      document.getElementById('authPanel').classList.remove('hidden');
      document.getElementById('appPanel').classList.add('hidden');
      document.getElementById('userChip').textContent = 'LIVE';
    }

    refreshProfile();
  </script>
</body>
</html>`);
});

app.listen(PORT, () => {
  console.log('V-Max Profit backend listening on port ' + PORT);
});
