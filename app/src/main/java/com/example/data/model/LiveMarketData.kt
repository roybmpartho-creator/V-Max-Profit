package com.example.data.model

import java.util.Locale

data class LiveFeedTx(
    val id: String,
    val userMasked: String,
    val type: String, // "DEPOSIT", "WITHDRAWAL", "CLAIM"
    val amount: Double,
    val network: String,
    val timeLabel: String
)

data class LeaderboardEarner(
    val rank: Int,
    val handle: String,
    val countryFlag: String,
    val avatarLevel: Int,
    val income: Double,
    val delta: Double = 0.0
)

object LiveMarketDataGenerator {
    private val flags = listOf("🇺🇸", "🇬🇧", "🇧🇩", "🇩🇪", "🇯🇵", "🇨🇦", "🇦🇺", "🇸🇬", "🇦🇪", "🇫🇷", "🇰🇷", "🇧🇷")
    private val prefixes = listOf(
        "ApexWhale", "CyberKing", "CryptoNinja", "MetaSatoshi", "EtherTitan",
        "NexusTrader", "VanguardApe", "TronBaron", "BinanceWhale", "RoyalSimian",
        "DeFiKnight", "AlphaMatrix", "GoldHodler", "NeonPhantom", "MatrixRunner",
        "SatoshiSon", "BlockMaster", "QuantumApe", "CyberSamurai", "CryptoBull"
    )

    fun generateInitial30Transactions(): List<LiveFeedTx> {
        val types = listOf("DEPOSIT", "WITHDRAWAL", "CLAIM", "DEPOSIT")
        val networks = listOf("Binance Pay V2", "BSC BEP20", "TRON TRC20", "Binance Auto Payout")
        val list = mutableListOf<LiveFeedTx>()
        for (i in 0 until 30) {
            val type = types.random()
            val amt = when (type) {
                "DEPOSIT" -> listOf(20.0, 50.0, 100.0, 250.0, 500.0, 1000.0, 1500.0, 2500.0).random()
                "WITHDRAWAL" -> listOf(35.0, 75.0, 120.0, 300.0, 750.0, 1250.0).random()
                else -> listOf(22.0, 55.0, 110.0, 275.0, 550.0).random()
            }
            list.add(
                LiveFeedTx(
                    id = "TX-" + (100000 + i * 37),
                    userMasked = "USR-" + (100 + i * 7) + "***",
                    type = type,
                    amount = amt,
                    network = networks.random(),
                    timeLabel = "${i * 2}s ago"
                )
            )
        }
        return list
    }

    fun generate100Earners(): List<LeaderboardEarner> {
        val high = 590100.0
        val low = 538377.0
        val step = (high - low) / 99.0
        return (0 until 100).map { i ->
            val baseIncome = high - (i * step) + ((-40..40).random().toDouble())
            val name = "${prefixes[i % prefixes.size]}_${String.format(Locale.US, "%03d", i + 1)}"
            LeaderboardEarner(
                rank = i + 1,
                handle = name,
                countryFlag = flags[i % flags.size],
                avatarLevel = (i % 9) + 1,
                income = baseIncome,
                delta = 0.0
            )
        }
    }
}
