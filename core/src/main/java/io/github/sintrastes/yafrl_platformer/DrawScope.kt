package io.github.sintrastes.yafrl_platformer

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion

interface DrawScope {
    fun texture(file: String): Texture

    fun texture(file: String, x: Int, y: Int, width: Int, height: Int): TextureRegion

    val batch: SpriteBatch
}
