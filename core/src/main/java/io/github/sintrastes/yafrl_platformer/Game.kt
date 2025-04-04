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

class Game(
    private val tileset: Texture,
    private val player: Texture
) {
    val clicked = broadcastEvent<Float2>("clicked")

    val clicks = State.fold(listOf<State<Entity>>(), clicked) { clicked, click ->
        clicked + entity(
            click,
            accelerating(Float2(0f, 220f), Float2(0f, 350f)),
        )
    }

    val spawned = clicks.flatMap { clicks ->
        clicks
            .sequenceState()
    }

    val tiles = const(
        listOf<Entity>(
            *(10..16).map {
                tile(Int2(it, 15))
            }.toTypedArray(),

            *(25..31).map {
                tile(Int2(it, 25))
            }.toTypedArray(),

            *(0..120).map { x ->
                tile(Int2(x, 36))
            }.toTypedArray()
        )
    )

    /**
     * Creates a speed [v] that is accelerating by [dv]
     **/
    fun accelerating(v: Float2, dv: Float2) = const(v) + integral(const(dv))

    fun entities() = State.combineAll(
        entity(
            Float2(GAME_SIZE.x / 2, 0f),
            accelerating(Float2(0f, 420f), Float2(0f, 350f)),
        ),
        entity(
            Float2(GAME_SIZE.x / 3, 100f),
            accelerating(Float2(0f, 430f), Float2(0f, 350f)),
        ),
        entity(
            Float2(2 * GAME_SIZE.x / 3, 50f),
            accelerating(Float2(0f, 440f), Float2(0f, 350f)),
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
//                drawImage(
//                    image = tileset,
//                    srcSize = Int2(16, 16),
//                    dstSize = Int2(tileSize.roundToPx(), tileSize.roundToPx()),
//                    srcOffset = IntOffset(5 * 16, 2 * 16),
//                    dstOffset = IntOffset(
//                        targetPosition.x.toInt(),
//                        targetPosition.y.toInt()
//                    ),
//                    filterQuality = FilterQuality.None
//                )
            }
        )
    }

    /** Creates a simple entity. */
    fun entity(
        start: Float2,
        speed: State<Float2>
    ): State<Entity> {
        val size = Float2(4 * 50f, 4 * 36f)

        val startVector = Float2(start.x, start.y)

        val position = speed.integrateWith(
            startVector,
            collisionSummation(size, tiles)
        )

        return position.map { position ->
            Entity(position, size) {
                batch.draw(
                    player,
                    0f,
                    0f,
                )

//                drawImage(
//                    image = player,
//                    srcSize = Int2(50, 36),
//                    dstSize = IntSize(4 * 50, 4 * 36),
//                    srcOffset = IntOffset(0, 0),
//                    dstOffset = IntOffset(
//                        position.x.toInt(),
//                        position.y.toInt()
//                    ),
//                    filterQuality = FilterQuality.None
//                )
            }
        }
    }

}
