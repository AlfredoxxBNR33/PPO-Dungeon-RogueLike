package br.com.ppo.Dungeon;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;

public class GameAudio {

    private static final float MONSTER_SOUND_RANGE = 400f;
    private static final float FOOTSTEPS_PITCH = 2f;

    private final Sound enemyExplosion = Gdx.audio.newSound(Gdx.files.internal("audio/enemy-explosion.mp3"));
    private final Sound footsteps = Gdx.audio.newSound(Gdx.files.internal("audio/Footsteps-Kaino.mp3"));
    private final Sound monsterGrowl = Gdx.audio.newSound(Gdx.files.internal("audio/monster-growl.mp3"));
    private final Sound rayGun = Gdx.audio.newSound(Gdx.files.internal("audio/ray-gun.mp3"));
    private long footstepsId = -1;

    public void playShot() {
        rayGun.play();
    }

    public void playEnemySpawn(float enemyX, float enemyY, float playerX, float playerY) {
        if (isNearPlayer(enemyX, enemyY, playerX, playerY)) {
            monsterGrowl.play();
        }
    }

    public void playEnemyDeath(float enemyX, float enemyY, float playerX, float playerY) {
        if (isNearPlayer(enemyX, enemyY, playerX, playerY)) {
            enemyExplosion.play();
        }
    }

    private boolean isNearPlayer(float sourceX, float sourceY, float playerX, float playerY) {
        float distanceX = sourceX - playerX;
        float distanceY = sourceY - playerY;
        return distanceX * distanceX + distanceY * distanceY <= MONSTER_SOUND_RANGE * MONSTER_SOUND_RANGE;
    }

    public void setFootstepsPlaying(boolean playing) {
        if (playing && footstepsId == -1) {
            footstepsId = footsteps.loop(1f, FOOTSTEPS_PITCH, 0f);
        } else if (!playing) {
            stopFootsteps();
        }
    }

    public void stopFootsteps() {
        if (footstepsId != -1) {
            footsteps.stop(footstepsId);
            footstepsId = -1;
        }
    }

    public void dispose() {
        stopFootsteps();
        enemyExplosion.dispose();
        footsteps.dispose();
        monsterGrowl.dispose();
        rayGun.dispose();
    }
}
