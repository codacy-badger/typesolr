package ch.acmesoftware.typesolr.core

import ch.acmesoftware.typesolr.core.Field.{DecodingError, FieldNotFound}
import org.apache.solr.common.SolrInputDocument

import scala.util.Either.RightProjection

case class Document(fields: Map[String, List[String]]) {

  def ~[T](f: Field[T])(implicit enc: FieldEncoder[T]): Document = withField(f)

  def withField[T](f: Field[T])(implicit enc: FieldEncoder[T]): Document = copy(fields + enc.encode(f))

  def field[T](key: String)(implicit dec: FieldDecoder[T]): Either[DecodingError, T] = fields.get(key).
    map(v => dec.decode(key, v).map(_.value)).
    getOrElse(Left(FieldNotFound(key)))

  def get[T](key: String)(implicit dec: FieldDecoder[T]): RightProjection[DecodingError, T] = field(key).right

  def asSolrInputDocument: SolrInputDocument = fields.foldLeft(new SolrInputDocument())((doc, f) => {
    val d2 = doc.deepCopy()
    f._2.foreach(d2.addField(f._1, _))
    d2
  })

  def size: Int = fields.size
}

object Document {
  def of[A](f: Field[A])(implicit enc: FieldEncoder[A]) = Document(Map(enc.encode(f)))
}