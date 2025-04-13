package io.github.sintrastes.yafrl_platformer

import io.github.sintrastes.yafrl.Behavior
import io.github.sintrastes.yafrl.vector.Float2
import io.github.sintrastes.yafrl.vector.VectorSpace

object Physics {
    /** Generic render-able entity. */
    data class Entity(
        val position: Float2,
        val size: Float2,
        val render: DrawScope.() -> Unit
    )

    /** Utility to check an entity at [tentativePosition] with size [entitySize] would collide with
     * an entity [other]. */
    fun collides(tentativePosition: Float2, entitySize: Float2, other: Entity): Boolean {
        val minX1 = tentativePosition.x
        val minY1 = tentativePosition.y
        val maxX1 = tentativePosition.x + entitySize.x
        val maxY1 = tentativePosition.y + entitySize.y

        val minX2 = other.position.x
        val minY2 = other.position.y
        val maxX2 = other.position.x + other.size.x
        val maxY2 = other.position.y + other.size.y

        return minX1 < maxX2 && maxX1 > minX2 &&
            minY1 < maxY2 && maxY1 > minY2
    }

    /**
     * Vector sum that clips the total summation if it would lead to a collision.
     **/
    fun collisionSummation(entitySize: Float2, entities: Behavior<List<Entity>>): (Float2, Float2) -> Float2 = with (VectorSpace.float2()) {
        return { accumulatedPosition, newVelocity ->
            val tentativePosition = accumulatedPosition + newVelocity

            val others = entities.value // Get current state of other entities

            var clippedPosition = tentativePosition
            for (other in others) {
                if (collides(tentativePosition, entitySize, other)) {
                    clippedPosition = clipPosition(tentativePosition, entitySize, newVelocity, other)
                }
            }

            clippedPosition
        }
    }

    /**
     * Utility to calculate the clipped position of an entity that is going to collide with
     *  an [other] entity.
     **/
    fun clipPosition(
        tentativePosition: Float2,
        entitySize: Float2, // Size of the moving entity
        velocity: Float2,
        other: Entity,
    ): Float2 {
        var clippedPosition = tentativePosition

        // Vertical clipping
        if (velocity.y > 0) { // Moving up
            // If the top overshoots other's bottom...
            if (tentativePosition.y + entitySize.y > other.position.y && tentativePosition.y < other.position.y) {
                // ...clip so that the top edge aligns with other's bottom edge.
                clippedPosition = clippedPosition.copy(y = other.position.y - entitySize.y)
            }
        } else if (velocity.y < 0) { // Moving down
            // If the bottom overshoots other's top...
            if (tentativePosition.y < other.position.y + other.size.y && tentativePosition.y + entitySize.y > other.position.y + other.size.y) {
                // ...clip so that the bottom edge aligns with other's top edge.
                clippedPosition = clippedPosition.copy(y = other.position.y + other.size.y)
            }
        }

        return clippedPosition
    }
}
