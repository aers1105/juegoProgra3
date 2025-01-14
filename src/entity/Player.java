package entity;

import control.Keyboard;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import main.GamePanel;

public class Player extends Entity {

    Keyboard keyboard;

    //Indicate where playe drop 
    public int screenX;
    public int screenY;
    public int hasKey, mana, maxMana;

    public Player(GamePanel gp, Keyboard keyboard) {
        super(gp);

        this.keyboard = keyboard;

        screenX = gp.screenWidth / 2 - (gp.tileSize / 2);
        screenY = gp.screenHeight / 2 - (gp.tileSize / 2);

        solidArea = new Rectangle();
        solidArea.x = 8;
        solidArea.y = 16;
        solidAreaDefaultX = solidArea.x;
        solidAreaDefaultY = solidArea.y;
        solidArea.width = 32;
        solidArea.height = 32;

        attackArea.width = 36;
        attackArea.height = 36;

        setDefaultValues();
        getPlayerImage();
        getPlayerAttackImage();
    }

    public void setDefaultValues() {
        setDefaultLifeAndAttack();
        setDefaultPossitionAndSpeed();
        setDefaultItems();
        invencible = false;
    }

    //Load the sprites.
    public void getPlayerImage() {

        up1 = setup("/images/player/Walking_sprites/boy_up_1", gp.tileSize, gp.tileSize);
        up2 = setup("/images/player/Walking_sprites/boy_up_2", gp.tileSize, gp.tileSize);
        down1 = setup("/images/player/Walking_sprites/boy_down_1", gp.tileSize, gp.tileSize);
        down2 = setup("/images/player/Walking_sprites/boy_down_2", gp.tileSize, gp.tileSize);
        left1 = setup("/images/player/Walking_sprites/boy_left_1", gp.tileSize, gp.tileSize);
        left2 = setup("/images/player/Walking_sprites/boy_left_2", gp.tileSize, gp.tileSize);
        right1 = setup("/images/player/Walking_sprites/boy_right_1", gp.tileSize, gp.tileSize);
        right2 = setup("/images/player/Walking_sprites/boy_right_2", gp.tileSize, gp.tileSize);
    }

    public void getPlayerAttackImage() {

        attackUp1 = setup("/images/player/Attacking_sprites/boy_attack_up_1", gp.tileSize, gp.tileSize * 2);
        attackUp2 = setup("/images/player/Attacking_sprites/boy_attack_up_2", gp.tileSize, gp.tileSize * 2);
        attackDown1 = setup("/images/player/Attacking_sprites/boy_attack_down_1", gp.tileSize, gp.tileSize * 2);
        attackDown2 = setup("/images/player/Attacking_sprites/boy_attack_down_2", gp.tileSize, gp.tileSize * 2);
        attackLeft1 = setup("/images/player/Attacking_sprites/boy_attack_left_1", gp.tileSize * 2, gp.tileSize);
        attackLeft2 = setup("/images/player/Attacking_sprites/boy_attack_left_2", gp.tileSize * 2, gp.tileSize);
        attackRight1 = setup("/images/player/Attacking_sprites/boy_attack_right_1", gp.tileSize * 2, gp.tileSize);
        attackRight2 = setup("/images/player/Attacking_sprites/boy_attack_right_2", gp.tileSize * 2, gp.tileSize);
    }

    //Steps and collision
    @Override
    public void update() {
        int speedPlayer = 4;
        int speedSprite = 12;
        getPlayerAttackImage();
        if (attacking) {
            attacking();
        }
        if (keyboard.pausePressed) {
                gp.gameState = gp.pauseState;
                attacking = false;
            }
        if (keyboard.savePressed) {

            try {
                gp.saveLoad.save();
                gp.ui.showMessage("Genial, guardé mi progreso.");
            } catch (IOException ex) {
                Logger.getLogger(Player.class.getName()).log(Level.SEVERE, null, ex);
            }
            keyboard.savePressed = false;
        }
        if (keyboard.upPressed
                || keyboard.downPressed
                || keyboard.leftPressed
                || keyboard.rightPressed
                || keyboard.swordPressed
                ) {
            

            if (keyboard.upPressed) {
                direction = "up";
                if (keyboard.runPressed) {
                    speedSprite = 5;
                    speedPlayer += 4;

                }

                attacking = false;
            } else if (keyboard.downPressed) {
                direction = "down";
                if (keyboard.runPressed) {
                    speedSprite = 5;
                    speedPlayer += 4;
                }

                attacking = false;
            } else if (keyboard.leftPressed) {
                direction = "left";
                if (keyboard.runPressed) {
                    speedSprite = 5;
                    speedPlayer += 4;
                }

                attacking = false;
            } else if (keyboard.rightPressed) {
                direction = "right";
                if (keyboard.runPressed) {
                    speedSprite = 5;
                    speedPlayer += 4;
                }

                attacking = false;
            }

            //Check tile collision
            collisonOn = false;
            int objIndex = gp.cChecker.checkObject(this, true);

            //Check NPC collision
            int NPC_Index = gp.cChecker.checkEntity(this, gp.npc);
            try {
                interactNPC(NPC_Index);
            } catch (IOException ex) {
            }

            //Check object collision
            gp.cChecker.checkTile(this);
            pickUpObject(objIndex);

            //Check monster collision
            int monster_index = gp.cChecker.checkEntity(this, gp.monsters);
            contactMonster(monster_index);

            //Check if player´s life is zero
            if (life == 0) {
                gp.gameState = gp.gameOverState;
            }

            //If collision is false, player can move 
            if (!collisonOn && !keyboard.swordPressed) {
                switch (direction) {
                    case "up":
                        worldY -= speedPlayer;
                        break;
                    case "down":
                        worldY += speedPlayer;
                        break;
                    case "left":
                        worldX -= speedPlayer;
                        break;
                    case "right":
                        worldX += speedPlayer;
                        break;

                }
            }

            spriteCounter++;
            if (spriteCounter > speedSprite) {
                if (spriteNumber == 1) {
                    spriteNumber = 2;
                } else if (spriteNumber == 2) {
                    spriteNumber = 1;
                }
                spriteCounter = 0;
            }

        }

        //this need to be outside of key statement
        if (invencible) {
            invencibleCounter++;
            if (invencibleCounter > 60) {
                invencible = false;
                invencibleCounter = 0;
            }
        }

    }

    public void interactNPC(int i) throws IOException {
        if (gp.keyboard.swordPressed) {
            if (i != 999) {
                gp.gameState = gp.dioalogueState;
                gp.npc[i].speak();

            } else {
                attacking = true;
            }

        }

    }

    public void pickUpObject(int index) {
        if (index != 999) {
            String objectName = gp.obj[index].name;

            switch (objectName) {
                case "Key":
                    gp.playSE(1);

                    gp.obj[index] = null;
                    if (hasKey == 0) {
                        gp.ui.showMessage("¿Una llave?");
                    } else {
                        gp.ui.showMessage("¿Otra llave?");
                    }
                    hasKey++;
                    break;
                case "Door":
                    if (hasKey > 0) {
                        gp.playSE(3);
                        gp.obj[index] = null;
                        hasKey--;

                    } else {
                        gp.playSE(5);
                        gp.ui.showMessage("¿Hum?... Creo que necisito una llave");
                    }
                    break;
                case "Chest":
                   
                    gp.gameState = gp.finishedState;
                    gp.ui.drawFinishGame();
                    gp.playSE(4);
                    break;

            }
        }
    }

    @Override
    public void draw(Graphics2D g2) {
        BufferedImage image = null;

        int tempScreenX = screenX;
        int tempScreenY = screenY;

        switch (direction) {
            case "up":
                if (!attacking) {
                    if (spriteNumber == 1) {
                        image = up1;
                    }
                    if (spriteNumber == 2) {

                        image = up2;
                    }
                }
                if (attacking) {
                    tempScreenY = screenY - gp.tileSize;
                    if (spriteNumber == 1) {
                        image = attackUp1;
                    }
                    if (spriteNumber == 2) {
                        image = attackUp2;
                    }
                }

                break;
            case "down":
                if (!attacking) {
                    if (spriteNumber == 1) {
                        image = down1;
                    }
                    if (spriteNumber == 2) {
                        image = down2;
                    }
                }
                if (attacking) {
                    if (spriteNumber == 1) {
                        image = attackDown1;
                    }
                    if (spriteNumber == 2) {
                        image = attackDown2;
                    }
                }
                break;
            case "left":
                if (!attacking) {
                    if (spriteNumber == 1) {
                        image = left1;
                    }
                    if (spriteNumber == 2) {
                        image = left2;
                    }
                }
                if (attacking) {
                    tempScreenX = screenX - gp.tileSize;
                    if (spriteNumber == 1) {
                        image = attackLeft1;
                    }
                    if (spriteNumber == 2) {
                        image = attackLeft2;
                    }
                }
                break;
            case "right":
                if (!attacking) {
                    if (spriteNumber == 1) {
                        image = right1;
                    }
                    if (spriteNumber == 2) {
                        image = right2;
                    }
                }
                if (attacking) {
                    if (spriteNumber == 1) {
                        image = attackRight1;
                    }
                    if (spriteNumber == 2) {
                        image = attackRight2;
                    }
                }
                break;
        }
        if (invencible) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
        }
        g2.drawImage(image, tempScreenX, tempScreenY, null);

        //Reset slphscomposite
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
    }

    public void contactMonster(int i) {
        if (i != 999) {
            if (!invencible) {
                life -= 1;
                invencible = true;
            }
        }
    }

    public void setDefaultPossitionAndSpeed() {
        worldX = gp.tileSize * 23;
        worldY = gp.tileSize * 21;
        speed = 4;
        direction = "down";
    }

    public void setDefaultLifeAndAttack() {
        maxLife = 6;
        life = maxLife;
        powerAttack = 1;
    }

    public void setDefaultItems() {
        hasKey = 0;
    }

    private void attacking() {
        //attacking = false;
        spriteCounter++;
        if (spriteCounter <= 5) {
            spriteNumber = 1;
        }
        if (spriteCounter > 5 && spriteCounter <= 25) {
            spriteNumber = 2;

            //Save the current worldX, worldY, solidArea.
            int currentWorldX = worldX;
            int currentWorldY = worldY;
            int solidAreaWidht = solidArea.width;
            int solidAreaHight = solidArea.height;

            //Adjust player´s worldX/Y for attackArea.
            switch (direction) {
                case "up":
                    worldY -= attackArea.height;
                    break;
                case "down":
                    worldY += attackArea.height;
                    break;
                case "left":
                    worldX -= attackArea.width;
                    break;
                case "right":
                    worldX += attackArea.width;
                    break;
            }
            //attackArea becomes solidArea.
            solidArea.width = attackArea.width;
            solidArea.height = attackArea.height;
            //check monster collison with the update  worldX, worldY and solidArea.
            int monsterIndex = gp.cChecker.checkEntity(this, gp.monsters);
            damageMonster(monsterIndex);

            //After checking collison, restore the original area.
            worldX = currentWorldX;
            worldY = currentWorldY;
            solidArea.width = solidAreaWidht;
            solidArea.height = solidAreaHight;

        }
        if (spriteCounter > 25) {
            spriteNumber = 1;
            spriteCounter = 0;
            attacking = false;

        }

    }

    private void damageMonster(int i) {

        if (i != 999) {

            if (gp.monsters[i].invencible == false) {

                gp.monsters[i].life -= 1;
                gp.monsters[i].invencible = true;

                if (gp.monsters[i].life <= 0) {
                    gp.monsters[i] = null;
                }
            }
        }

    }

}
