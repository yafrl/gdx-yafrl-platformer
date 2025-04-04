package io.github.sintrastes.yafrl_platformer

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch

interface DrawScope {
    fun texture(file: String): Texture

    val batch: SpriteBatch
}
