package io.github.sintrastes.yafrl_platformer

import com.badlogic.gdx.graphics.Texture
import io.github.sintrastes.yafrl.Behavior.Companion.integral
import io.github.sintrastes.yafrl.State
import io.github.sintrastes.yafrl.State.Companion.const
import io.github.sintrastes.yafrl.broadcastEvent
import io.github.sintrastes.yafrl.integrateWith
import io.github.sintrastes.yafrl.plus
import io.github.sintrastes.yafrl.sequenceState
import io.github.sintrastes.yafrl.vector.Float2
import io.github.sintrastes.yafrl_platformer.Physics.Entity
import io.github.sintrastes.yafrl_platformer.Physics.collisionSummation

val GAME_SIZE = Float2(1000f, 1000f)

val TILE_HEIGHT = 32f

data class Int2(val x: Int, val y: Int)

class Game() {
    val clicked = broadcastEvent<Float2>("clicked")

    val clicks = State.fold(listOf<State<Entity>>(), clicked) { clicked, click ->
        clicked + entity(
            click,
            accelerating(Float2(0f, -220f), Float2(0f, -350f)),
        )
    }

    val spawned = clicks.flatMap { clicks ->
        clicks
            .sequenceState()
    }

    val tiles = const(
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
    fun accelerating(v: Float2, dv: Float2) = const(v) + integral(const(dv))

    fun entities() = State.combineAll(
        entity(
            Float2(GAME_SIZE.x / 2, 500f),
            accelerating(Float2(0f, -100f), Float2(0f, -200f)),
        ),
        entity(
            Float2(GAME_SIZE.x / 3, 200f),
            accelerating(Float2(0f, -100f), Float2(0f, -200f)),
        ),
        entity(
            Float2(2 * GAME_SIZE.x / 3, 350f),
            accelerating(Float2(0f, -100f), Float2(0f, -200f)),
        )
    ).combineWith(spawned) { initial, spawned ->
        initial + spawned
    }.combineWith(tiles) { entities, tiles ->
        tiles + entities
    }

    /** Creates a tile */
    fun tile(
        position: Int2
    ): Entity {
        val targetPosition = Float2(position.x * TILE_HEIGHT, position.y * TILE_HEIGHT)
        return Entity(
            position = targetPosition,
            size = Float2(TILE_HEIGHT, TILE_HEIGHT),
            render = {
                val tile = this.texture("tileset.png", 5 * 16, 2 * 16, 16, 16)

                batch.draw(
                    tile,
                    targetPosition.x,
                    targetPosition.y,
                    TILE_HEIGHT,
                    TILE_HEIGHT
                )
            }
        )
    }

    /** Creates a simple entity. */
    fun entity(
        start: Float2,
        speed: State<Float2>
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
                    texture("adventurer-idle-00.png"),
                    position.x,
                    position.y,
                    size.x,
                    size.y
                )
            }
        }
    }
}
