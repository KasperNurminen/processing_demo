class Force {
  float x, y;

  Force(float xForce, float yForce) {
    x = xForce;
    y = yForce;
  }

  void addForce(Force another) {
    /* Adds two forces */
    x += another.x;
    y += another.y;
  }

  Force multiply(float scale) {
    /* multiplies the x- and y-forces by a float */
    return new Force(x * scale, y * scale);
  }
}