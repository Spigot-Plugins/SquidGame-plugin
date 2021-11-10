# SquidGame-plugin
## How to install
### Maven
Add the JitPack repository to your build file:
<br>
```xml
<repositories>
  <repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
  </repository>
</repositories>
```
Add the dependency:
<br>
```xml
<dependency>
    <groupId>com.github.unldenis</groupId>
    <artifactId>SquidGame-plugin</artifactId>
    <version>master-81955c7be4-1</version>
    <scope>provided</scope>
</dependency>
```
### Gradle
Add the JitPack repository to your build file: 
<br>
```xml
allprojects {
  repositories {
    ...
    maven { url 'https://jitpack.io' }
  }
}
```
Add the dependency:
<br>
```xml
implementation 'com.github.unldenis:SquidGame-plugin:master-81955c7be4-1'
```
