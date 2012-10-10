import sbt._
import Keys._
import PlayProject._
import com.github.play2war.plugin._

object ApplicationBuild extends Build {

    val appName         = "skyjam"
    val appVersion      = "1.0-SNAPSHOT"

    val appDependencies = Seq(
      // Add your project dependencies here,
		"mysql" % "mysql-connector-java" % "5.1.18",
		"be.objectify" %% "deadbolt-2" % "1.1.2",
		"com.typesafe" %% "play-plugins-mailer" % "2.0.4"

    )
    
	val projectSettings = Play2WarPlugin.play2WarSettings ++ Seq(
	  // Your settings
	  Play2WarKeys.servletVersion := "3.0",
	  resolvers += Resolver.url("Objectify Play Repository", url("http://schaloner.github.com/releases/"))(Resolver.ivyStylePatterns)
	)    

    val main = PlayProject(appName, appVersion, appDependencies, mainLang = JAVA).settings(
			projectSettings: _* 
    )
    

}
