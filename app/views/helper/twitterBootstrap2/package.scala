package views.html.helper

/**
 * Contains template helpers, for example for generating HTML forms.
 */
package object twitterBootstrap2 {

  /**
   * Twitter bootstrap 2 input structure.
   *
   */
  implicit val twitterBootstrapField = new FieldConstructor {
    def apply(elts: FieldElements) = twitterBootstrap2FieldConstructor(elts)
  }

}