# NFT TRADE — Backend & Companion Website Deployment Guide

এই গাইডে সম্পূর্ণ Backend সার্ভার সেটআপ, Binance Pay V2 কানেকশন, Nodemailer OTP, এবং মোবাইল অ্যাপের সাথে Companion Website (Next.js/React/HTML) বানানোর সম্পূর্ণ স্টেপ-বাই-স্টেপ নির্দেশনা দেওয়া হয়েছে।

---

## ১. Node.js Backend ফাইল স্ট্রাকচার

```
/backend
├── server.js              # Express REST API, Binance Pay V2, Nodemailer OTP, SSE Stream
├── package.json           # ডিপেন্ডেন্সি তালিকা
├── .env.example           # সিক্রেট কনফিগারেশন টেমপ্লেট
└── models/
    ├── User.js            # MongoDB ইউজার স্কিমা
    └── Transaction.js     # ডিপোজিট ও উইথড্র ট্রানজ্যাকশন স্কিমা
```

---

## ২. Backend সার্ভার রান করার নিয়ম (How to Run Backend)

### লোকাল মেশিনে রান করতে:
```bash
cd backend
npm install
cp .env.example .env
# .env ফাইলে আপনার Gmail App Password এবং Binance Pay Credentials দিন
npm start
```
সার্ভার চালু হবে: `http://localhost:5000` এ।

### Render / Railway / VPS এ ফ্রি হোস্টিং করার নিয়ম:
1. **GitHub** এ আপনার কোড পুশ করুন।
2. [Render.com](https://render.com) বা [Railway.app](https://railway.app) এ লগইন করে **New Web Service** সিলেক্ট করুন।
3. `backend` ডিরেক্টরি সিলেক্ট করে **Environment Variables** এ `.env.example` এর কীগুলো বসিয়ে দিন।
4. ডিপ্লয় বাটনে ক্লিক করলেই আপনার লাইভ API URL পেয়ে যাবেন (e.g. `https://nft-trade-api.onrender.com`)।

---

## ৩. গুরুত্বপূর্ণ API এন্ডপয়েন্টসমূহ (API Endpoints)

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/auth/register` | নাম, ইমেইল, অ্যাড্রেস দিয়ে অ্যাকাউন্ট তৈরি ও ৬-ডিজিট OTP পাঠানো |
| POST | `/api/auth/verify-otp` | ৬-ডিজিট OTP ভেরিফাই করে অ্যাকাউন্ট অ্যাক্টিভ করা |
| POST | `/api/auth/login` | স্মার্ট লগইন (`roybmpartho@gmail.com` -> Admin, বাকিরা -> User) |
| POST | `/api/binance/create-order` | Binance Pay V2 অর্ডার, QR কোড ও Checkout URL জেনারেট করা |
| POST | `/api/binance/webhook` | Binance এর পেমেন্ট কনফার্মেশন পেয়ে ইউজারের ব্যালেন্স + ৫% বোনাস যোগ করা |
| POST | `/api/wallet/withdraw` | $১ ফিক্সড ফি কেটে স্বয়ংক্রিয় Binance Auto Payout এক্সিকিউট করা |
| GET | `/api/stream/live-feed` | প্রতি মিনিটে ৩০টি লাইভ ট্রানজ্যাকশন স্ট্রিমিং (প্রতি ২ সেকেন্ডে ১টি) |
| GET | `/api/admin/users` | সমস্ত রেজিস্ট্রার্ড ইউজার ও তাদের KYC স্ট্যাটাস দেখা |
| POST | `/api/admin/adjust-balance` | ইউজারের ব্যালেন্স ম্যানুয়ালি যোগ বা বিয়োগ করা |

---

## ৪. অ্যাপের সাথে Website বানাতে চাইলে যা যা করতে হবে (Website Roadmap)

আপনি যদি এই Android অ্যাপটির সাথে একটি **Commercial NFT TRADE Website** বানাতে চান, তবে নিচের ৩টি ধাপ অনুসরণ করুন:

### ধাপ ১: Frontend ফ্রেমওয়ার্ক নির্বাচন (Next.js / React)
Next.js অথবা React.js ব্যবহার করে খুব দ্রুত একটি প্রিমিয়াম ডার্ক-থিম ওয়েবসাইট তৈরি করা যায়।
- **Pages**:
  1. **Home (`/`)**: Hero Monkey NFT Banner, 24/7 Live Ticker, Available/Holding Balance, 124+ Countries, Top NFT Holder, Live 30 tx/min feed, Top 100 Earner Leaderboard।
  2. **Claim (`/claim`)**: অ্যাক্টিভ NFT হোল্ডিংস ও ম্যাচিওরড পেআউট ক্লেইম।
  3. **Marketplace (`/nft`)**: ৯টি Cyber Ape NFT কালেকশন কেনাবেচা।
  4. **Deposit & Withdraw (`/wallet`)**: Binance Pay V2 পেমেন্ট গেটওয়ে এবং $১ ফিক্সড ফি অটো উইথড্রয়াল।
  5. **Admin Panel (`/admin`)**: Roy Bm Partho এর জন্য ডেডিকেটেড ব্যালেন্স কন্ট্রোল, ডিপোজিট ভেরিফিকেশন ও ইউজার ব্যান/আনব্যান ম্যানেজমেন্ট।

### ধাপ ২: Backend API কানেক্ট করা
ওয়েবসাইটের কোডে সরাসরি আপনার ডিপ্লয় করা Node.js সার্ভারের সাথে কানেক্ট করবেন:
```javascript
const API_BASE = "https://your-backend-api.onrender.com";

// ডিপোজিট অর্ডার তৈরি:
const res = await fetch(`${API_BASE}/api/binance/create-order`, {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ userId: user.id, amount: 50 })
});
const { data } = await res.json();
// data.checkoutUrl এবং data.qrImageUrl ইউজারকে দেখানো হবে
```

### ধাপ ৩: ডোমেইন ও ফ্রি হোস্টিং
1. [Vercel.com](https://vercel.com) এ আপনার ওয়েবসাইটটি ফ্রি হোস্টিং করতে পারবেন।
2. আপনার কাস্টম ডোমেইন (যেমন `nfttrade.io` বা `nfttrade.com`) Vercel এ যুক্ত করে দিলেই ওয়েবসাইট লাইভ হয়ে যাবে!
