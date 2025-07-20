package me.weishu.kernelsu.ui.animation

import androidx.compose.animation.core.Easing
import androidx.compose.runtime.Immutable
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin
import kotlin.math.sqrt

@Immutable
class SpringEasing @JvmOverloads constructor(
    private val damping: Float = 0.95f,
    private val response: Float = 0.5f,
    private val mass: Float = 1.0f,
    private val acceleration: Float = 0.0f
) : Easing {
    var duration: Long = 1000L
        private set
    private var g = 0.0
    private var inputScale = 1.0f
    private var omega = 0.0
    private var p = 0.0
    private var q = 0.0
    private lateinit var solution: SpringSolution
    private var velocity = 0.0f
    private var xStar = 0.0
    private var zeta = 0.0

    init {
        updateParameters()
    }

    override fun transform(fraction: Float): Float {
        if (fraction == 1.0f) {
            return 1.0f
        }
        val f2 = fraction * this.inputScale
        val x = solution.x(f2).toFloat()
        this.velocity = solution.dX(f2).toFloat()
        return x
    }

    private fun updateParameters() {
        val d = damping.toDouble()
        this.zeta = d
        val d2 = 6.283185307179586 / this.response
        this.omega = d2
        val f = this.mass
        val d3 = (((d * 2.0) * d2) * f) / f
        this.p = d3
        val d4 = ((d2 * d2) * f) / f
        this.q = d4
        val d5 = acceleration.toDouble()
        this.g = d5
        val d6 = ((-d5) / d4) + 1.0
        this.xStar = d6
        val d7 = (d3 * d3) - (d4 * 4.0)
        val d8 = 0.0 - d6
        this.solution = if (d7 > 0.0) {
            OverDampingSolution(d7, d8, d3, velocity.toDouble(), d6)
        } else if (d7 == 0.0) {
            CriticalDampingSolution(d8, d3, velocity.toDouble(), d6)
        } else {
            UnderDampingSolution(d7, d8, d3, velocity.toDouble(), d6)
        }
        val solveDuration = (solveDuration(d7) * 1000.0).toLong()
        this.duration = solveDuration
        this.inputScale = (solveDuration.toFloat()) / 1000.0f
    }

    private fun solveDuration(d: Double): Double {
        var d2: Double
        var d3 = 0.0
        val d4 = if (d >= 0.0) 0.001 else 1.0E-4
        val d5 = this.g
        var d6 = 1.0
        if (d5 == 0.0) {
            var f = 0.0f
            while (abs(d3 - 1.0) > d4) {
                f += 0.001f
                d3 = solution.x(f)
                val dX = solution.dX(f)
                if (abs(d3 - 1.0) <= d4 && dX <= 5.0E-4) {
                    break
                }
            }
            return f.toDouble()
        }
        val solve = solution.solve(0.0, this.q, d5, this.xStar)
        val d7 = this.q
        val d8 = this.xStar
        val d9 = d7 * d8 * d8
        val d10 = (solve - d9) * d4
        var d11 = 1.0
        var solve2 = solution.solve(1.0, d7, this.g, d8)
        var d12 = 0.0
        while (true) {
            d2 = d9 + d10
            if (solve2 <= d2) {
                break
            }
            val d13 = d11 + d6
            d12 = d11
            d6 = 1.0
            d11 = d13
            solve2 = solution.solve(d13, this.q, this.g, this.xStar)
        }
        do {
            val d14 = (d12 + d11) / 2.0
            if (solution.solve(d14, this.q, this.g, this.xStar) > d2) {
                d12 = d14
            } else {
                d11 = d14
            }
        } while (d11 - d12 >= d4)
        return d11
    }

    internal abstract inner class SpringSolution {
        abstract fun dX(f: Float): Double

        abstract fun x(f: Float): Double

        fun solve(d: Double, d2: Double, d3: Double, d4: Double): Double {
            val f = d.toFloat()
            val x = x(f)
            val dX = dX(f)
            return (((d2 * x) * x) + (dX * dX)) - ((d3 * 2.0) * (x - d4))
        }
    }

    internal inner class CriticalDampingSolution(
        d2: Double,
        d3: Double,
        d4: Double,
        d5: Double
    ) :
        SpringSolution() {
        private val c1: Double
        private val c2: Double
        private val r: Double
        private val xStar: Double

        init {
            val d6 = (-d3) / 2.0
            this.r = d6
            this.c1 = d2
            this.c2 = d4 - (d2 * d6)
            this.xStar = d5
        }

        override fun x(f: Float): Double {
            val d = f.toDouble()
            return ((this.c1 + (this.c2 * d)) * exp(this.r * d)) + this.xStar
        }

        override fun dX(f: Float): Double {
            val d = this.c1
            val d2 = this.r
            val d3 = this.c2
            val d4 = f.toDouble()
            return ((d * d2) + (d3 * ((d2 * d4) + 1.0))) * exp(d2 * d4)
        }
    }

    internal inner class OverDampingSolution(
        d: Double,
        d2: Double,
        d3: Double,
        d4: Double,
        d5: Double
    ) :
        SpringSolution() {
        private val c1: Double
        private val c2: Double
        private val r1: Double
        private val r2: Double
        private val xStar: Double

        init {
            val sqrt = sqrt(d)
            val d6 = (sqrt - d3) / 2.0
            this.r1 = d6
            val d7 = ((-sqrt) - d3) / 2.0
            this.r2 = d7
            this.c1 = (d4 - (d2 * d7)) / sqrt
            this.c2 = (-(d4 - (d6 * d2))) / sqrt
            this.xStar = d5
        }

        override fun x(f: Float): Double {
            val d = f.toDouble()
            return (this.c1 * exp(this.r1 * d)) + (this.c2 * exp(this.r2 * d)) + this.xStar
        }

        override fun dX(f: Float): Double {
            val d = this.c1
            val d2 = this.r1
            val d3 = f.toDouble()
            val exp = d * d2 * exp(d2 * d3)
            val d4 = this.c2
            val d5 = this.r2
            return exp + (d4 * d5 * exp(d5 * d3))
        }
    }

    internal inner class UnderDampingSolution(
        d: Double,
        d2: Double,
        d3: Double,
        d4: Double,
        d5: Double
    ) :
        SpringSolution() {
        private val alpha: Double
        private val beta: Double
        private val c1: Double
        private val c2: Double
        private val xStar: Double

        init {
            val d6 = (-d3) / 2.0
            this.alpha = d6
            val sqrt = sqrt(-d) / 2.0
            this.beta = sqrt
            this.c1 = d2
            this.c2 = (d4 - (d2 * d6)) / sqrt
            this.xStar = d5
        }

        override fun x(f: Float): Double {
            val d = f.toDouble()
            return (exp(this.alpha * d) * ((this.c1 * cos(this.beta * d)) + (this.c2 * sin(
                this.beta * d
            )))) + this.xStar
        }

        override fun dX(f: Float): Double {
            val d = f.toDouble()
            val exp = exp(this.alpha * d)
            val d2 = this.c1 * this.alpha
            val d3 = this.c2
            val d4 = this.beta
            val cos = (d2 + (d3 * d4)) * cos(d4 * d)
            val d5 = this.c2 * this.alpha
            val d6 = this.c1
            val d7 = this.beta
            return exp * (cos + ((d5 - (d6 * d7)) * sin(d7 * d)))
        }
    }
}