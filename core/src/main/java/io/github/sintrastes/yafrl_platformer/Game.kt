package io.github.sintrastes.yafrl_platformer

import io.github.sintrastes.yafrl.Behavior
import io.github.sintrastes.yafrl.Behavior.Companion.integral
import io.github.sintrastes.yafrl.Event
import io.github.sintrastes.yafrl.State
import io.github.sintrastes.yafrl.Behavior.Companion.const
import io.github.sintrastes.yafrl.integrate
import io.github.sintrastes.yafrl.integrateWith
import io.github.sintrastes.yafrl.plus
import io.github.sintrastes.yafrl.not
import io.github.sintrastes.yafrl.sequenceState
import io.github.sintrastes.yafrl.vector.Float2
import io.github.sintrastes.yafrl_platformer.Physics.Entity
import io.github.sintrastes.yafrl_platformer.Physics.collisionSummation
import kotlin.math.max

val GAME_SIZE = Float2(1000f, 1000f)

val TILE_HEIGHT = 32f

data class Int2(val x: Int, val y: Int)

class Game(
    val clicked: Event<Float2>,
    val a: Event<Unit>,
    val d: Event<Unit>,
    val space: Event<Unit>
) {
    private val spawned = State.fold(listOf<State<Entity>>(), clicked) { clicked, click ->
        clicked + entity(
            click,
            accelerating(const(Float2(0f, -220f)), Float2(0f, -350f), -700f),
        )
    }

    // Note: For some reason, adding these is messing up motion.
    val facingRight = Event.merged(
        a.map { false },
        d.map { true }
    )
        // TODO: Why does this get messed up if I replace it with hold?
        .scan(true) { x, y -> y }

    private val spawnedStates = spawned.flatMap { spawned ->
        spawned
            .sequenceState()
    }

    private val tiles = State.const(
        listOf<Entity>(
            *(10..16).map {
                tile(Int2(it, 10))
            }.toTypedArray(),

            *(25..31).map {
                tile(Int2(it, 15))
            }.toTypedArray(),

            *(0..120).map { x ->
                tile(Int2(x, 0))
            }.toTypedArray()
        )
    )

    /**
     * Creates a speed [v] that is accelerating by [dv]
     **/
    private fun accelerating(
        v: Behavior<Float2>,
        dv: Float2,
        terminal: Float
    ) = (v + integral(const(dv))).map { velocity ->
        Float2(velocity.x, max(terminal, velocity.y))
    }

    val playerVelocity: Behavior<Float2> by lazy {
        val velocity = a.impulse(Float2(0f, 0f), Float2(-3.5f, 0f)) +
                d.impulse(Float2(0f, 0f), Float2(3.5f, 0f)) +
                space.gate(!onGround).impulse(Float2(0f, 0f), Float2(0f, 350f))

        val acceleration = Float2(0f, -550f)

        accelerating(
            velocity,
            acceleration,
            -300f
        )
    }

    val player by lazy {
        entity(
            Float2(GAME_SIZE.x / 2, 500f),
            playerVelocity
        )
    }

    val onGround by lazy {
        Behavior.continuous {
            val player = player.value
            val tiles = tiles.value

            val playerLeft = player.position.x
            val playerRight = player.position.x + player.size.x

            tiles
                .any { tile ->
                    val tileLeft = tile.position.x
                    val tileRight = tile.position.x + TILE_HEIGHT

                    player.position.y == tile.position.y + TILE_HEIGHT &&
                            playerRight > tileLeft && !(playerLeft > tileRight)
                }
        }
    }

    val entities = State.combineAll<Entity>(
//        entity(
//            Float2(GAME_SIZE.x / 2, 500f),
//            accelerating(Float2(0f, -100f), Float2(0f, -200f)),
//        )
    ).combineWith(spawnedStates) { initial, spawned ->
        initial + spawned
    }.combineWith(player) { entities, player ->
        entities + player
    }.combineWith(tiles) { entities, tiles ->
        tiles + entities
    }

    /** Creates a tile */
    private fun tile(
        position: Int2
    ): Entity {
        val targetPosition = Float2(position.x * TILE_HEIGHT, position.y * TILE_HEIGHT)
        return Entity(
            position = targetPosition,
            size = Float2(TILE_HEIGHT, TILE_HEIGHT),
            render = {
                batch.draw(
                    texture("tileset.png", 5 * 16, 2 * 16, 16, 16),
                    targetPosition.x,
                    targetPosition.y,
                    TILE_HEIGHT,
                    TILE_HEIGHT
                )
            }
        )
    }

    /** Creates a simple entity. */
    private fun entity(
        start: Float2,
        speed: Behavior<Float2>
    ): State<Entity> {
        val size = Float2(2 * 19f, 2 * 29f)

        val startVector = Float2(start.x, start.y)

        val position = speed.integrateWith(
            startVector,
            collisionSummation(size, tiles)
        )

        return position.map { position ->
            Entity(position, size) {
                batch.draw(
                    if (facingRight.value) {
                        texture("adventurer-idle-00.png")
                    } else {
                        texture("adventurer-idle-00-left.png")
                    },
                    position.x,
                    position.y,
                    size.x,
                    size.y
                )
            }
        }
    }
}
