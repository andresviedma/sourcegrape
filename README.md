# sourcegrape
Groovy source code dependency manager. Similar to @Grab annotation for grapes, it provides a @SourceGrab annotation that uses source code automatically downloaded and updated from Git.

Example of use:

```groovy
// Load SourceGrape artifact from the Maven repo
@GrabResolver(name='bintray-andresviedma-maven', root='http://dl.bintray.com/andresviedma/maven')
@Grab('com.sourcegrape:sourcegrape')
import com.sourcegrape.*

// Load the sample source, including the dummy TestClass class
@SourceGrab('https://github.com/andresviedma/groovy-assets-directory-example.git')

// Is TestClass there?
TestClass test = new TestClass()
test.helloWorld()
```
