package authenticator


trait From[A, B] { self =>
  def read(in: A): B

  def andThen[C](from: From[B, C]): From[A, C] = new From[A, C] {
    override def read(in: A): C = from.read(self.read(in))
  }

  def mapFrom[C](f: B => C): From[A, C] = new From[A, C] {
    override def read(in: A): C = f(self.read(in))
  }

  def asFrom: From[A, B] = self
}


trait To[A, B] { self =>
  def write(in: A): B

  def andThen[C](to: To[B, C]): To[A, C] = new To[A, C]{
    override def write(in: A): C = to.write(self.write(in))
  }

  def mapTo[C](f: B => C): To[A, C] = new To[A, C] {
    override def write(in: A): C = f(self.write(in))
  }

  def asTo: To[A, B] = self
}

