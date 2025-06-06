package jadx.plugins.input.apkm

import jadx.api.ResourceFile
import jadx.api.ResourcesLoader
import jadx.api.plugins.CustomResourcesLoader
import jadx.api.plugins.utils.CommonFileUtils
import jadx.zip.ZipReader
import java.io.File

class ApkmCustomResourcesLoader(
	private val zipReader: ZipReader,
) : CustomResourcesLoader {
	private val tmpFiles = mutableListOf<File>()

	override fun load(loader: ResourcesLoader, list: MutableList<ResourceFile>, file: File): Boolean {
		if (!file.name.endsWith(".apkm")) return false

		// Check if this is a valid APKM file
		val manifest = ApkmUtils.getManifest(file, zipReader) ?: return false
		if (!ApkmUtils.isSupported(manifest)) return false

		// Load all files ending with .apk
		zipReader.visitEntries<Any>(file) { entry ->
			if (entry.name.endsWith(".apk")) {
				val tmpFile = entry.inputStream.use {
					CommonFileUtils.saveToTempFile(it, ".apk").toFile()
				}
				loader.defaultLoadFile(list, tmpFile, entry.name + "/")
				tmpFiles += tmpFile
			}
			null
		}
		return true
	}

	override fun close() {
		tmpFiles.forEach(CommonFileUtils::safeDeleteFile)
		tmpFiles.clear()
	}
}
