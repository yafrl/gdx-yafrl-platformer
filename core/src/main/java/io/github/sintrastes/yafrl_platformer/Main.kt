package io.github.sintrastes.yafrl_platformer

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.ScreenUtils
import com.badlogic.gdx.utils.viewport.ScreenViewport
import io.github.sintrastes.yafrl.State
import io.github.sintrastes.yafrl.BroadcastEvent
import io.github.sintrastes.yafrl.broadcastEvent
import io.github.sintrastes.yafrl.internal.Timeline
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class Main : ApplicationAdapter(), DrawScope {
    private lateinit var _batch: SpriteBatch

    private lateinit var background: Texture
    private lateinit var player: Texture
    private lateinit var tileset: Texture

    private lateinit var rootScope: CoroutineScope
    private lateinit var clock: BroadcastEvent<Duration>
    private lateinit var timeline: Timeline
    private lateinit var game: Game
    private lateinit var entities: State<List<Physics.Entity>>

    val camera = OrthographicCamera()
    val viewport = ScreenViewport(camera)

    override val batch: SpriteBatch
        get() = _batch

    val textures = mutableMapOf<String, Texture>()

    override fun texture(file: String): Texture {
        if (!textures.contains(file)) {
            textures[file] = Texture(file)
        }

        return textures[file]!!
    }

    override fun create() {
        _batch = SpriteBatch()
        background = Texture("background.png")
        player = Texture("adventurer-idle-00.png")
        tileset = Texture("tileset.png")

        rootScope = CoroutineScope(Dispatchers.Default)

        Timeline.initializeTimeline(
            rootScope,
            initClock = {
                clock
            }
        )

        clock = broadcastEvent()

        timeline = Timeline.currentTimeline()

        game = Game(
            tileset = tileset,
            player = player
        )

        entities = game.entities()
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height, true)
    }

    override fun render() {
        val dt = Gdx.graphics.deltaTime.toDouble().seconds
        clock.send(dt)

        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f)
        camera.update()
        batch.projectionMatrix = camera.combined

        batch.begin()
        batch.draw(background, 0f, 0f,
            Gdx.graphics.width.toFloat(),
            Gdx.graphics.height.toFloat()
        )
        batch.draw(player, 0f, 0f, 4 * 50f, 4 * 36f)
        batch.end()
    }

    override fun dispose() {
        batch.dispose()
        background.dispose()
        for (texture in textures.values) {
            texture.dispose()
        }
    }
}
