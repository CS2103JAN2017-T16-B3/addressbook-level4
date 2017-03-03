# AddressBook Level 4 - Developer Guide

By : `Team SE-EDU`  &nbsp;&nbsp;&nbsp;&nbsp; Since: `Jun 2016`  &nbsp;&nbsp;&nbsp;&nbsp; Licence: `MIT`

---

1. [Setting Up](#setting-up)
2. [Design](#design)
3. [Implementation](#implementation)
4. [Testing](#testing)
5. [Dev Ops](#dev-ops)

* [Appendix A: User Stories](#appendix-a--user-stories)
* [Appendix B: Use Cases](#appendix-b--use-cases)
* [Appendix C: Non Functional Requirements](#appendix-c--non-functional-requirements)
* [Appendix D: Glossary](#appendix-d--glossary)
* [Appendix E : Product Survey](#appendix-e--product-survey)


## 1. Setting up

### 1.1. Prerequisites

1. **JDK `1.8.0_60`**  or later<br>

    > Having any Java 8 version is not enough. <br>
    This app will not work with earlier versions of Java 8.

2. **Eclipse** IDE
3. **e(fx)clipse** plugin for Eclipse (Do the steps 2 onwards given in
   [this page](http://www.eclipse.org/efxclipse/install.html#for-the-ambitious))
4. **Buildship Gradle Integration** plugin from the Eclipse Marketplace
5. **Checkstyle Plug-in** plugin from the Eclipse Marketplace


### 1.2. Importing the project into Eclipse

0. Fork this repo, and clone the fork to your computer
1. Open Eclipse (Note: Ensure you have installed the **e(fx)clipse** and **buildship** plugins as given
   in the prerequisites above)
2. Click `File` > `Import`
3. Click `Gradle` > `Gradle Project` > `Next` > `Next`
4. Click `Browse`, then locate the project's directory
5. Click `Finish`

  > * If you are asked whether to 'keep' or 'overwrite' config files, choose to 'keep'.
  > * Depending on your connection speed and server load, it can even take up to 30 minutes for the set up to finish
      (This is because Gradle downloads library files from servers during the project set up process)
  > * If Eclipse auto-changed any settings files during the import process, you can discard those changes.

### 1.3. Configuring Checkstyle
1. Click `Project` -> `Properties` -> `Checkstyle` -> `Local Check Configurations` -> `New...`
2. Choose `External Configuration File` under `Type`
3. Enter an arbitrary configuration name e.g. addressbook
4. Import checkstyle configuration file found at `config/checkstyle/checkstyle.xml`
5. Click OK once, go to the `Main` tab, use the newly imported check configuration.
6. Tick and select `files from packages`, click `Change...`, and select the `resources` package
7. Click OK twice. Rebuild project if prompted

> Note to click on the `files from packages` text after ticking in order to enable the `Change...` button

### 1.4. Troubleshooting project setup

**Problem: Eclipse reports compile errors after new commits are pulled from Git**

* Reason: Eclipse fails to recognize new files that appeared due to the Git pull.
* Solution: Refresh the project in Eclipse:<br>
  Right click on the project (in Eclipse package explorer), choose `Gradle` -> `Refresh Gradle Project`.

**Problem: Eclipse reports some required libraries missing**

* Reason: Required libraries may not have been downloaded during the project import.
* Solution: [Run tests using Gradle](UsingGradle.md) once (to refresh the libraries).


## 2. Design

### 2.1. Architecture

<img src="images/Architecture.png" width="600"><br>
_Figure 2.1.1 : Architecture Diagram_

The **_Architecture Diagram_** given above explains the high-level design of the App.
Given below is a quick overview of each component.

> Tip: The `.pptx` files used to create diagrams in this document can be found in the [diagrams](diagrams/) folder.
> To update a diagram, modify the diagram in the pptx file, select the objects of the diagram, and choose `Save as picture`.

`Main` has only one class called [`MainApp`](../src/main/java/seedu/address/MainApp.java). It is responsible for,

* At app launch: Initializes the components in the correct sequence, and connects them up with each other.
* At shut down: Shuts down the components and invokes cleanup method where necessary.

[**`Commons`**](#common-classes) represents a collection of classes used by multiple other components.
Two of those classes play important roles at the architecture level.

* `EventsCenter` : This class (written using [Google's Event Bus library](https://github.com/google/guava/wiki/EventBusExplained))
  is used by components to communicate with other components using events (i.e. a form of _Event Driven_ design)
* `LogsCenter` : Used by many classes to write log messages to the App's log file.

The rest of the App consists of four components.

* [**`UI`**](#ui-component) : The UI of the App.
* [**`Logic`**](#logic-component) : The command executor.
* [**`Model`**](#model-component) : Holds the data of the App in-memory.
* [**`Storage`**](#storage-component) : Reads data from, and writes data to, the hard disk.

Each of the four components

* Defines its _API_ in an `interface` with the same name as the Component.
* Exposes its functionality using a `{Component Name}Manager` class.

For example, the `Logic` component (see the class diagram given below) defines it's API in the `Logic.java`
interface and exposes its functionality using the `LogicManager.java` class.<br>
<img src="images/LogicClassDiagram.png" width="800"><br>
_Figure 2.1.2 : Class Diagram of the Logic Component_

#### Events-Driven nature of the design

The _Sequence Diagram_ below shows how the components interact for the scenario where the user issues the
command `delete 1`.

<img src="images\SDforDeletePerson.png" width="800"><br>
_Figure 2.1.3a : Component interactions for `delete 1` command (part 1)_

>Note how the `Model` simply raises a `AddressBookChangedEvent` when the Address Book data are changed,
 instead of asking the `Storage` to save the updates to the hard disk.

The diagram below shows how the `EventsCenter` reacts to that event, which eventually results in the updates
being saved to the hard disk and the status bar of the UI being updated to reflect the 'Last Updated' time. <br>
<img src="images\SDforDeletePersonEventHandling.png" width="800"><br>
_Figure 2.1.3b : Component interactions for `delete 1` command (part 2)_

> Note how the event is propagated through the `EventsCenter` to the `Storage` and `UI` without `Model` having
  to be coupled to either of them. This is an example of how this Event Driven approach helps us reduce direct
  coupling between components.

The sections below give more details of each component.

### 2.2. UI component

Author: Alice Bee

<img src="images/UiClassDiagram.png" width="800"><br>
_Figure 2.2.1 : Structure of the UI Component_

**API** : [`Ui.java`](../src/main/java/seedu/address/ui/Ui.java)

The UI consists of a `MainWindow` that is made up of parts e.g.`CommandBox`, `ResultDisplay`, `PersonListPanel`,
`StatusBarFooter`, `BrowserPanel` etc. All these, including the `MainWindow`, inherit from the abstract `UiPart` class.

The `UI` component uses JavaFx UI framework. The layout of these UI parts are defined in matching `.fxml` files
 that are in the `src/main/resources/view` folder.<br>
 For example, the layout of the [`MainWindow`](../src/main/java/seedu/address/ui/MainWindow.java) is specified in
 [`MainWindow.fxml`](../src/main/resources/view/MainWindow.fxml)

The `UI` component,

* Executes user commands using the `Logic` component.
* Binds itself to some data in the `Model` so that the UI can auto-update when data in the `Model` change.
* Responds to events raised from various parts of the App and updates the UI accordingly.

### 2.3. Logic component

Author: Bernard Choo

<img src="images/LogicClassDiagram.png" width="800"><br>
_Figure 2.3.1 : Structure of the Logic Component_

**API** : [`Logic.java`](../src/main/java/seedu/address/logic/Logic.java)

1. `Logic` uses the `Parser` class to parse the user command.
2. This results in a `Command` object which is executed by the `LogicManager`.
3. The command execution can affect the `Model` (e.g. adding a person) and/or raise events.
4. The result of the command execution is encapsulated as a `CommandResult` object which is passed back to the `Ui`.

Given below is the Sequence Diagram for interactions within the `Logic` component for the `execute("delete 1")`
 API call.<br>
<img src="images/DeletePersonSdForLogic.png" width="800"><br>
_Figure 2.3.1 : Interactions Inside the Logic Component for the `delete 1` Command_

### 2.4. Model component

Author: Cynthia Dharman

<img src="images/ModelClassDiagram.png" width="800"><br>
_Figure 2.4.1 : Structure of the Model Component_

**API** : [`Model.java`](../src/main/java/seedu/address/model/Model.java)

The `Model`,

* stores a `UserPref` object that represents the user's preferences.
* stores the Address Book data.
* exposes a `UnmodifiableObservableList<ReadOnlyPerson>` that can be 'observed' e.g. the UI can be bound to this list
  so that the UI automatically updates when the data in the list change.
* does not depend on any of the other three components.

### 2.5. Storage component

Author: Darius Foong

<img src="images/StorageClassDiagram.png" width="800"><br>
_Figure 2.5.1 : Structure of the Storage Component_

**API** : [`Storage.java`](../src/main/java/seedu/address/storage/Storage.java)

The `Storage` component,

* can save `UserPref` objects in json format and read it back.
* can save the Address Book data in xml format and read it back.

### 2.6. Common classes

Classes used by multiple components are in the `seedu.addressbook.commons` package.

## 3. Implementation

### 3.1. Logging

We are using `java.util.logging` package for logging. The `LogsCenter` class is used to manage the logging levels
and logging destinations.

* The logging level can be controlled using the `logLevel` setting in the configuration file
  (See [Configuration](#configuration))
* The `Logger` for a class can be obtained using `LogsCenter.getLogger(Class)` which will log messages according to
  the specified logging level
* Currently log messages are output through: `Console` and to a `.log` file.

**Logging Levels**

* `SEVERE` : Critical problem detected which may possibly cause the termination of the application
* `WARNING` : Can continue, but with caution
* `INFO` : Information showing the noteworthy actions by the App
* `FINE` : Details that is not usually noteworthy but may be useful in debugging
  e.g. print the actual list instead of just its size

### 3.2. Configuration

Certain properties of the application can be controlled (e.g App name, logging level) through the configuration file
(default: `config.json`):


## 4. Testing

Tests can be found in the `./src/test/java` folder.

**In Eclipse**:

* To run all tests, right-click on the `src/test/java` folder and choose
  `Run as` > `JUnit Test`
* To run a subset of tests, you can right-click on a test package, test class, or a test and choose
  to run as a JUnit test.

**Using Gradle**:

* See [UsingGradle.md](UsingGradle.md) for how to run tests using Gradle.

We have two types of tests:

1. **GUI Tests** - These are _System Tests_ that test the entire App by simulating user actions on the GUI.
   These are in the `guitests` package.

2. **Non-GUI Tests** - These are tests not involving the GUI. They include,
   1. _Unit tests_ targeting the lowest level methods/classes. <br>
      e.g. `seedu.address.commons.UrlUtilTest`
   2. _Integration tests_ that are checking the integration of multiple code units
     (those code units are assumed to be working).<br>
      e.g. `seedu.address.storage.StorageManagerTest`
   3. Hybrids of unit and integration tests. These test are checking multiple code units as well as
      how the are connected together.<br>
      e.g. `seedu.address.logic.LogicManagerTest`

#### Headless GUI Testing
Thanks to the [TestFX](https://github.com/TestFX/TestFX) library we use,
 our GUI tests can be run in the _headless_ mode.
 In the headless mode, GUI tests do not show up on the screen.
 That means the developer can do other things on the Computer while the tests are running.<br>
 See [UsingGradle.md](UsingGradle.md#running-tests) to learn how to run tests in headless mode.

### 4.1. Troubleshooting tests

 **Problem: Tests fail because NullPointException when AssertionError is expected**

 * Reason: Assertions are not enabled for JUnit tests.
   This can happen if you are not using a recent Eclipse version (i.e. _Neon_ or later)
 * Solution: Enable assertions in JUnit tests as described
   [here](http://stackoverflow.com/questions/2522897/eclipse-junit-ea-vm-option). <br>
   Delete run configurations created when you ran tests earlier.

## 5. Dev Ops

### 5.1. Build Automation

See [UsingGradle.md](UsingGradle.md) to learn how to use Gradle for build automation.

### 5.2. Continuous Integration

We use [Travis CI](https://travis-ci.org/) and [AppVeyor](https://www.appveyor.com/) to perform _Continuous Integration_ on our projects.
See [UsingTravis.md](UsingTravis.md) and [UsingAppVeyor.md](UsingAppVeyor.md) for more details.

### 5.3. Publishing Documentation

See [UsingGithubPages.md](UsingGithubPages.md) to learn how to use GitHub Pages to publish documentation to the
project site.

### 5.4. Making a Release

Here are the steps to create a new release.

 1. Generate a JAR file [using Gradle](UsingGradle.md#creating-the-jar-file).
 2. Tag the repo with the version number. e.g. `v0.1`
 2. [Create a new release using GitHub](https://help.github.com/articles/creating-releases/)
    and upload the JAR file you created.

### 5.5. Converting Documentation to PDF format

We use [Google Chrome](https://www.google.com/chrome/browser/desktop/) for converting documentation to PDF format,
as Chrome's PDF engine preserves hyperlinks used in webpages.

Here are the steps to convert the project documentation files to PDF format.

 1. Make sure you have set up GitHub Pages as described in [UsingGithubPages.md](UsingGithubPages.md#setting-up).
 1. Using Chrome, go to the [GitHub Pages version](UsingGithubPages.md#viewing-the-project-site) of the
    documentation file. <br>
    e.g. For [UserGuide.md](UserGuide.md), the URL will be `https://<your-username-or-organization-name>.github.io/addressbook-level4/docs/UserGuide.html`.
 1. Click on the `Print` option in Chrome's menu.
 1. Set the destination to `Save as PDF`, then click `Save` to save a copy of the file in PDF format. <br>
    For best results, use the settings indicated in the screenshot below. <br>
    <img src="images/chrome_save_as_pdf.png" width="300"><br>
    _Figure 5.4.1 : Saving documentation as PDF files in Chrome_

### 5.6. Managing Dependencies

A project often depends on third-party libraries. For example, Address Book depends on the
[Jackson library](http://wiki.fasterxml.com/JacksonHome) for XML parsing. Managing these _dependencies_
can be automated using Gradle. For example, Gradle can download the dependencies automatically, which
is better than these alternatives.<br>
a. Include those libraries in the repo (this bloats the repo size)<br>
b. Require developers to download those libraries manually (this creates extra work for developers)<br>

## Appendix A : User Stories

Priorities: High (must have) - `* * *`, Medium (nice to have)  - `* *`,  Low (unlikely to have) - `*`


Priority | As a ... | I want to ... | So that I can...
-------- | :-------- | :--------- | :-----------
`* * *` | new user | see usage instructions | refer to instructions when I forget how to use the App
`* * *` | new user | view more information about a particular command | so that I can learn how to use various commands
`* * *` | user | set a task by specifying a deadline | know when the task is due
`* * *` | user | add a task with a start time and end time | record tasks that are events
`* * *` | user | add a task by specifying a task description only | record tasks that need to be done ��some day��
`* * *` | user | delete a task | get rid of tasks that I no longer care to track
`* * *` | user | mark a task as done so that the task will not appear in my to-do list anymore
`* * *` | user | unmark tasks previously marked as done to edit the task status
`* * *` | user | edit the deadline of a specific task when the deadline of a task changes
`* * *` | user | edit the task descriptions | change the descriptions when I get new information
`* * *` | user | list tasks that are due within the day, week or month | have an overview of my schedule and decide what needs to be done soon
`* * *` | user | search and list out all the tasks containing the keyword(s)
`* * *` | user | undo my most recent action so that my mistakes are not permanent
`* * *` | advanced user | use shorter versions of a command | type a command faster
`* * *` | advanced user | specify which folder I want to save the files in | have easy access to the tasks just by sharing the files
`* *` | user | set tasks to repeat over a specified interval | manage recurring tasks
`* *` | user | add additional details or subtasks to a task | record tasks in detail
`* *` | user | expand or collapse the additional details or subtasks of a task | prevent the task list from becoming very cluttered
`* *` | user | add location details to events | I know where the event is taking place
`* *` | user | be notified if the time period an event I am adding clashes or overlaps with another event already added | reschedule the event to another free time slot if needed
`* *` | user | assign tags to the tasks | organise them properly
`* *` | user | indicate the priority of a task | see which tasks are more urgent or important
`* *` | user | add a ��VERY IMPORTANT�� Task to be shown whenever I open up the task list
`* *` | user | colour code my tasks | I can differentiate tasks better
`* *` | user | add icons to my tasks | quickly tell what kind of tasks I have
`* *` | user | organise my tasks into different sections | view only the tasks that are relevant to the situation
`* *` | user | pin tasks such that they automatically go to the top of the list and stay there
`* *` | user | list the deadline tasks by date | know which are the most urgent.
`* *` | user | list tasks by tags | see what are the tasks under a specific category. 
`* *` | user | list tasks by priority | know which are the most urgent tasks. 
`* *` | user | view the list of tasks that I have completed | unmark completed tasks if necessary.
`* *` | user | reorder my tasks | reorder floating tasks.
`* *` | user | search better with auto complete | search better.
`* *` | user | search for empty time periods | schedule my tasks with minimal overlap or clashes in deadlines.
`* *` | user | receive flavour text when I mark a task as complete such as ��Good job!�� And ��Another one off the list!��
`* *` | user | receive sound effects when I mark a task as completed | give myself more motivation to complete my tasks.
`* *` | user | share my completed task on social media | let my friends know if I have accomplished something I'm proud of. 
`* *` | user | export the tasks to a calendar file for use with other apps.
`* *` | user | sync my task list with my other devices | access my task list easily.
`* *` | user | sync the tasks with my email | create tasks automatically from incoming emails.
`* *` | user | set up email notifications for specific tasks | get email reminders for when a task is due soon. 
`* *` | user | enable auto spell checker to correct any spelling mistakes I might make when typing commands.
`* *` | advanced user | change the layout of my UI (eg. background colour, font size) | customize it according to my preference.
`* *` | advanced user | add default keywords to my interface | customize it according to the vocabulary that I am most comfortable with. 
`* *` | advanced user | be able to use shortcut keys to execute commands that I commonly use so that I can do things faster and more efficiently. (Eg. Ctrl+z for undoing)
`* *` | advanced user | view a log of my history so that I can track all the commands I have entered since the start of time.
`*` | user that works with a lot of other people | share my task list with other people | view and work on the tasks as a group.


## Appendix B : Use Cases

(For all use cases below, the **System** is the `AddressBook` and the **Actor** is the `user`, unless specified otherwise)

#### Use case: Delete person

**MSS**

1. User requests to list persons
2. AddressBook shows a list of persons
3. User requests to delete a specific person in the list
4. AddressBook deletes the person <br>
Use case ends.

**Extensions**

2a. The list is empty

> Use case ends

3a. The given index is invalid

> 3a1. AddressBook shows an error message <br>
  Use case resumes at step 2

{More to be added}

## Appendix C : Non Functional Requirements

1. Should work on any [mainstream OS](#mainstream-os) as long as it has Java `1.8.0_60` or higher installed.
2. Should be able to hold up to 1000 persons without a noticeable sluggishness in performance for typical usage.
3. A user with above average typing speed for regular English text (i.e. not code, not system admin commands)
   should be able to accomplish most of the tasks faster using commands than using the mouse.

{More to be added}

## Appendix D : Glossary

##### Mainstream OS

> Windows, Linux, Unix, OS-X

##### Private contact detail

> A contact detail that is not meant to be shared with others

## Appendix E : Product Survey

**Todoist**

Pros:

* Supports many variations of keywords for time and dates (eg. next week, 3 days, everyday etc.)
* Shortcut keys such as for adding and finding allow use of mouse clicks to be minimized 
* Auto-complete when adding tags helps to increase efficiency of usage
* Can add a variety of tags (category, priority) to tasks to organize them better 
* Powerful search feature that can find tasks given minimal keywords, and also find tasks by their deadlines 
* Good visual feedback through colour coding for tasks with different priorities and categories
* Karma trend and goals enable user to track productivity and stay motivated to complete tasks on time 

Cons:

* Requires Internet connection and web browser to use
* Requires logging in to account which means there is an extra step to launch the software 
* Certain features (eg. sync with calendar, customize theme) are only available with paid subscription
* Most other functions are not command line interface friendly (Eg. edit, mark as complete require mouse clicks)
* No help feature to guide new users to keywords like date and time. Need to refer to the user guide.
* If user backspaces after typing a keyword but retypes it again for the same task, the date will not be registered anymore, which gives little allowance if users type wrongly. 

**HiTask**

Pros:

* Good GUI especially the calendar that automatically highlights dates with tasks due/ events scheduled, which makes it easy for user to find an empty time slot to schedule a new task if needed.
* Nice and clear GUI display when listing tasks for the day, week or month which come with a time-line so that task cards height are proportional to the task duration.

Cons:

* Software requires Internet connection and not available as a desktop application
* Requires an account to use and is only free for a limited period
* Not very command line interface friendly

**Any.do**

Pros:

* Able to be synced between mobile and computer as tasks can be managed either through going to the website on a browser and/or downloading and using the mobile application.
* Clean and simple UI design for anti clutter.
* Lists can be sorted by manually entered types followed by urgency (Today, tomorrow, upcoming, someday)
* Premium only: Able to change the colour theme.
* Premium only: Able to manage as a group
* Premium only: Able to attach notes and files to tasks
* Premium only: Able to assign tasks to group members
* Luckily, premium is quite affordable at $1.49/month
* Able to add notes and subtasks

Cons:

* Quite a number of features require premium account.
* Requires Internet connection to sync tasks.
* Limited options to customise UI.

**Wunderlist**

Pros:

* Able to repeat tasks with customisable frequency (days, weeks, months, years)
* Able to add subtaskes, notes and files
* Can star a task with the option of making it automatically go to the top of the list
* List can be subsumed under folders
* Can enable sound effects for notifications or upon completion of a task
* User is able to make a Feature Request to Wunderlist
* Able to snooze reminders for a customisable time
* Able to have email, push and desktop notifications
* Able to sync due dates with any calendar that supports the iCalendar format
* Has both a mobile and desktop application that can work offline
* Able to duplicate list
* Able to customise shortcuts 
* Able to specify preferred formats for the data, time and start of the week
* Able to restore deleted lists
* Able to print list

Cons:
* Priority is based on due dates only, tasks cannot be sorted into broad priority categories
* Lacks colour coding of tasks

