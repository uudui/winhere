import static groovy.io.FileType.FILES
import org.codehaus.groovy.grails.plugins.GrailsPluginUtils
import org.codehaus.groovy.grails.plugins.GrailsPluginInfo

databaseChangeLog = {
	//�˴������Ѿ���װ��hi���,���в��Ĭ�Ͼ���hi��ʼ���磺hi-fwk, hi-ui��
	def hiPlugins = [:]
	def dataFiles = []

	def pluginInfos = GrailsPluginUtils.pluginInfos
	for (GrailsPluginInfo info in pluginInfos) {
		if (info.name.startsWith('hi-')) {
			hiPlugins[info.name] = info.pluginDir
		}
	}
	println("HiPlugins:"+hiPlugins)
	//����������е�migration�ļ�include����
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
		//������CSV�ļ����Ƶ����е���Ŀ
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
	//��data�µ�csv�ļ�ɾ��
//    dataFiles.each {
//        it.delete()
//    }

	//����Ŀ�����migration�ļ�include����
	def migrationFolder = new File(System.getProperty("user.dir") + "/grails-app/migrations")
	if (migrationFolder.exists()) {
		migrationFolder.eachFileRecurse(FILES) {
			if(it.name.endsWith('-migration.groovy') && !it.name.endsWith('changelog.groovy')) {
				include(file: it.path, relativeToChangelog: true)
			}
		}
	}

}
