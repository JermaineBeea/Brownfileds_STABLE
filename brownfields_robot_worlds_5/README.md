# 🤖 RobotWorld

## 🌎 Brief Overview 

RobotWorld is a game-like simulation where users launch robots into an alien world. Robots move and interact within a shared environment filled with obstacles. Each robot can move, turn, shoot, repair itself, and reload—actions controlled by a set of commands that keep everything running smoothly. The world keeps track of robot and obstacle positions, what each robot can see, and how they affect each other during the game.

The main parts of the project include the Robot class, which defines how each robot behaves and what it can do; the World class, which manages the environment and all the robots; and the CommandHandler, which takes care of processing and executing the commands. There’s also a system for obstacles and visibility that influences movement and interactions.

---

## 🚀 Getting Started
This project is a `Java` project using `maven` as the build tool.

### 📦 3rd Party Dependencies  
- [🧩 JSON (org.json)](https://mvnrepository.com/artifact/org.json/json) – Used for creating and parsing responses


### 🛠 Installing `maven`  

Install `maven` using your favorite package manager:

#### 🍏 macOS (Using [Homebrew](https://brew.sh))

1. Install:  
   ```bash
   brew install maven
   ```

2. Verify Installation:  
   ```bash
   mvn -v
   ```

#### 🐧 Linux

##### 🐧 Ubuntu/Debian-based

1. Install:  
   ```bash
   sudo apt update && sudo apt install maven
   ```

2. Verify Installation:  
   ```bash
   mvn -v
   ```

#### 🪟 Windows (Using [Scoop](https://scoop.sh))

1. Install:
   - Open PowerShell as Administrator  
   - Run:  
     ```powershell
     scoop install maven
     ```

2. Verify Installation:  
   ```bash
   mvn -v
   ```

### 🌐 Getting Local IP Address (Wi-Fi) **Needed for running RobotClient**

- *Note 1*: Run these commands on the machine that will be the server
- *Note 2*: The client and server need to be on the same local network for it to work

#### 🍏 macOS
#### Via Terminal:
```bash
ipconfig getifaddr en0
$ Output (e.g.): 192.168.4.31 
```


### 🪟 Windows
#### Via Command Prompt (CMD):
```cmd
for /f "tokens=14" %a in ('ipconfig ^| findstr "IPv4"') do @echo %a
$ Output (e.g.): 192.168.4.31 

```

### 🐧 Linux
#### Via Terminal:

```bash
hostname -I
$ Output (e.g.): 192.168.4.31 
```
- Returns all assigned IPs. The first is usually your local IP.

### 🏗 Building the RobotServer from CLI 

1. Clone the [📁 repository](https://gitlab.wethinkco.de/kumangajhb024/oop-ex-toy-robot-group)  
2. Navigate to the root folder:  
   ```bash
   cd <your-cloned-folder>
   ```
3. Clean and compile:  
   ```bash
   mvn clean compile
   ```
4. Build and run the RobotServer:  
   ```bash
   mvn exec:java -Dexec.mainClass="za.co.wethinkcode.robots.server.Server"
   ```
5. Enter a port number when prompted (e.g. `3000`)


### 🏗 Building a RobotClient from CLI

1. Clone the [📁 repository](https://gitlab.wethinkco.de/kumangajhb024/oop-ex-toy-robot-group)  
2. Navigate to the root folder:  
   ```bash
   cd <your-cloned-folder>
   ```
3. Clean and compile:  
   ```bash
   mvn clean compile
   ```
4. Build and run the RobotServer:  
   ```bash
  	mvn exec:java -Dexec.mainClass="za.co.wethinkcode.robots.client.ClientApp"
   ```
5. Enter a IP address of the server (e.g. `192.168.4.31`)
6. Enter the port number of the server (e.g. `3000`)

### ✅ Running Unit Tests
1. Clone the [📁 repository](https://gitlab.wethinkco.de/kumangajhb024/oop-ex-toy-robot-group)  
2. Navigate to the root folder:  
   ```bash
   cd <your-cloned-folder>
   ```
3. Test:  
   ```bash
   mvn test
   ```
