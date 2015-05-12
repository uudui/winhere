import static groovy.io.FileType.FILES
import org.codehaus.groovy.grails.plugins.GrailsPluginUtils
import org.codehaus.groovy.grails.plugins.GrailsPluginInfo

databaseChangeLog = {
	//此处所有已经安装的hi插件,所有插件默认均以hi开始，如：hi-fwk, hi-ui等
	def hiPlugins = [:]
	def dataFiles = []

	def pluginInfos = GrailsPluginUtils.pluginInfos
	for (GrailsPluginInfo info in pluginInfos) {
		if (info.name.startsWith('hi-')) {
			hiPlugins[info.name] = info.pluginDir
		}
	}
	println("HiPlugins:"+hiPlugins)
	//将插件中所有的migration文件include进来
//    hiPlugins.each{ k, v ->
//        def migrationFolder = new File(v.toString() + "/grails-app/migrations")
//        if (migrationFolder.exists()) {
//            migrationFolder.eachFileRecurse(FILES) {
//                if(it.name.endsWith('-migration.groovy')) {
//                    println("Migration:"+it.path)
//                    include(file: it.path, relativeToChangelog: true)
//                }
//            }
//        }
//    }

	hiPlugins.each{ k, v ->
		//将所有CSV文件复制到运行的项目
		def dataFolder = new File(v.toString() + "/grails-app/migrations/data")
		if (dataFolder.exists()) {
			def toFolder = new File(System.getProperty('user.dir') + "/grails-app/migrations/data/")
			if(!toFolder.exists()) {
				toFolder.mkdir()
			}
			dataFolder.eachFileRecurse(FILES) {
				if(it.canRead()) {
					def dataFile = new File("${toFolder}/${it.name}")
					output = dataFile.newOutputStream()
					it.eachByte(1024, {data, lenth ->
						output.write(data, 0, lenth)
					})
					output.close()
					dataFiles << dataFile
				}
			}
		}



		def migrationFolder = new File(v.toString() + "/grails-app/migrations")
		if (migrationFolder.exists()) {
			migrationFolder.eachFileRecurse(FILES) {
				if(it.name.endsWith('-data.groovy')) {
					println("Migration:"+it.path)
					include(file: it.path, relativeToChangelog: true)
				}
			}
		}
	}
	//将data下的csv文件删除
//    dataFiles.each {
//        it.delete()
//    }

	//将项目本身的migration文件include进来
	def migrationFolder = new File(System.getProperty("user.dir") + "/grails-app/migrations")
	if (migrationFolder.exists()) {
		migrationFolder.eachFileRecurse(FILES) {
			if(it.name.endsWith('-migration.groovy') && !it.name.endsWith('changelog.groovy')) {
				include(file: it.path, relativeToChangelog: true)
			}
		}
	}

}
