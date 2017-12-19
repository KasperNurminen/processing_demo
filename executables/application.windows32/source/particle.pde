float forceMultiplier = 10000;
/* the minimum distance that the particle forces influence each other
/* this is needed because otherwise particles that are very close to each other would have insane 
/* amounts of energy */
float minDistance = 10; 
float maxForce = 100000; // upper cap to possible force amount
float particleDistance = 50; // distance where pull force transforms into push force


class Particle {
  PVector pos, vel;
  float brightness, colorB, colorC, mass;
  boolean isBigParticle;
  Particle(float x, float y, float particle_mass) {
    pos = new PVector(x, y);
    vel = new PVector(0, 0);
    brightness = random(0.4, 1);
    mass = particle_mass; 

    /* color stuff, creating a color scheme */
    float a = random(-1, 4);
    colorB = exp(a);
    colorC = exp(a/2);

    if (abs(mass) > 20) {
      isBigParticle = true;
    } else {
      isBigParticle = false;
    }
  }

  void drawYourself() {
    noStroke();
    /* creating a color scheme */
    fill(map(brightness, 0, 1, 0, 255), 
      map(pow(brightness, colorB), 0, 1, 0, 255), 
      map(pow(brightness, colorC), 0, 1, 32, 160));

    if (isBigParticle) { // the sipila
      image(img, this.pos.x-drawDiameter()/2, this.pos.y-drawDiameter()/2, drawDiameter(), drawDiameter());
    } else { // other particles
      ellipse(this.pos.x, this.pos.y, drawDiameter(), drawDiameter());
    }
  }

  float drawDiameter() {
    /* diameter = absolute value of the mass squared times 10 */
    return sqrt(abs(mass)) * 10;
  }

  void accelerate(Force force, float deltaTime) {
    /* calculate the current velocity */
    float multiplier = deltaTime / sqrt(mass) / 3;
    vel.x += force.x * multiplier;
    vel.y += force.y * multiplier;
  }

  void move(float deltaTime) {
    /* calculate the current position */
    pos.x += vel.x * deltaTime;
    pos.y += vel.y * deltaTime;

    // warp around the screen edges
    if (!isBigParticle) { // the sipila warps differently
      pos.x = (pos.x % width + width) % width;
      pos.y = (pos.y % height + height) % height;
    } else {
      if (pos.x > width*2) {
        vel.x = -abs(vel.x);
      }
      if (pos.x < -width) {
        vel.x = abs(vel.y);
      }
      if (pos.y > height*2) {
        vel.y = -abs(vel.y);
      }
      if (pos.y < -width) {
        vel.y = abs(vel.y);
      }
    }
  }

  void slowDown() {
    /* simulation of friction */
    vel.x *= 1 - 1/(1+mass*10);
    vel.y *= 1 - 1/(1+mass*10);
  }

  Force forceCausedByAnothedParticle(Particle another)  {
    /* calculations for the forces */
    float distance = dist(this.pos.x, this.pos.y, another.pos.x, another.pos.y);
    distance = max(distance, minDistance);

    // calculate the unit vectors
    float differenceNormalizedX = (another.pos.x - this.pos.x) / distance;
    float differenceNormalizedY = (another.pos.y - this.pos.y) / distance;
    Force forceDirection = new Force(differenceNormalizedX, differenceNormalizedY);

    float force = 0;
    // calculate the scalar part of the force 
    if (!another.isBigParticle) {
      force += forceMultiplier / pow(distance, 2);
      force -= forceMultiplier * particleDistance / pow(distance, 3);
    } else {
      // the sipila has a different kind of force
      distance -= another.drawDiameter() / 2 * 0.85;
      force += 3 * forceMultiplier / pow(distance, 2);
    }
    // the bigger the particle, the more force it has
    force *= another.mass;
    // ensure that the force is within the limits
    force = min(force, maxForce);
    force = max(force, -maxForce);

    // combine the unit vector with the scalar force
    return forceDirection.multiply(force);
  }
}