# Useful code snippets


## Save real response files for unit testing 
```kotlin
private fun <T : TransmissionRequest> saveResponseForTest(string: String, request: T) {
        val filename = request::class.simpleName
        val file = Files.createFile(FileSystems.getDefault().getPath("C:\\Users\\udell\\Documents\\IdeaProjects\\kotlin-transmission-client_github\\src\\test\\resources", "$filename.json"))
        file.toFile().writeText(string)
    }

```