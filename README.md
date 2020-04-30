# Vivas Schedule

This repository holds the documentation and code of the project developed for TAP (Técnicas Avançadas de Programação) curricular unit of MEI (Mestrado em Engenharia Informática) at ISEP (Instituto Superior de Engenharia do Porto). It addresses the topic of the scheduling of MSc Disseration Defenses (entitled as Vivas) based on the availability of the jury participants (referenced as resources), following the functional programming paradigm.

The development was divided in three milestones, being the first one an MVP implementation of the scheduling algorithm, the second one the assurance of the domain correctness with the use of property-based tests and the third one, a refinement to the scheduling algorithm with resources availability maximation. Additionality, coding guidelines were also defined to in order to control the quality of the code and facilitate the communication between developers.

## Coding Guidelines

To improve the quality of the software being produced, one must adopt and define a set of coding rules that assemble the coding guidelines of the project. Not only these rules assure that the code being written follows specific patterns, it also improves the readability and comprehension of the code and thus facilitating the communication of the developers. Given this, the following rules have to be complied when writing code:

- Only write code that complies with the principle of [substitution model](http://bkpathak.github.io/scala-substitution-model). This means that functions cannot have side effects (e.g no var declarations, no exception throwing, no I/O) as they only work with what is given to them.
- Don't over-engineer. Think twice before designing and writing the code of your solution. Simple is difficult.
- Don't re-engineer. Whenever possible take advantage of Scala APIs.
- Design for reusability and maintainability with Functional Programming (FP) techniques such as High Order Functions (HOF) and Pattern Matching. In the same way that you pass functions as arguments in HOF, inject all non-function dependencies to your function via parameters. Always work with only that is the input of the function.
- Declare domain classes as ADT (Algebraic Data Types) and with the use of smart constructors validate the domain rules specific to the class. If the validation is successful return `Success` with the instance of the class else return `Failure` that embeds an `IllegalArgumentException` with a proper message that indicates the domain validation error.
- Unit test the conditions of your functions.
- Enhance your domain correctness with the use of property-based testing. Design exhaustive generators that match *real* (expected) data to test the behavior of your functions.
- Use [scalatest](http://www.scalatest.org/) as the unit testing framework.
- Use [scalacheck](https://www.scalacheck.org/) as the property-based testing framework.
- Use [Mockito](https://site.mockito.org/) for mocking dependencies and enhancing your test value and experience. Scalatest already provides [support for Mockito](http://www.scalatest.org/user_guide/testing_with_mock_objects#mockito) so take advantage of it.
- Adopt a TDD approach when implementing functional code. It eases the implementation so much, as with the tests already defined, **you know the inputs, outputs and behavior** of your functions, and with this you just need to adapt your implementation to support the expected behavior.
- Adopt the AAA (Arrange-Act-Assert) pattern when designing unit tests as it improves the readability and comprehension of the test by dividing the test body in three phases.
- Take advantage of the [Matchers](http://www.scalatest.org/user_guide/using_matchers) API of `scalatest` in unit tests as it improves the readability and comprehension of the test by providing a rich grammar of comparison connectors specific of the testing domain.
- Recur to [scala-xml](https://www.scala-lang.org/api/2.9.3/scala/xml/XML$.html) API for serializing, deserializing and manipulating XML documents.
- Recur to `LocalDateTime` and `OffsetDateTime` APIs for date and time representations and operations. They are well tested implementations and are compatible with scala-xml API.
- Use [sbt](https://www.scala-sbt.org/) as the build tool.
- Use [WartRemover](https://www.wartremover.org/) as the linter tool. It gives you very good insights regarding your code and how to improve it in the domain of functional programming.
- Use [Scala Style Guide](https://docs.scala-lang.org/style/) as the guide for project structure and naming conventions.
- Use [scalafmt](https://scalameta.org/scalafmt/) as the code formatter. IntelliJ provides support to instantly format your code while you write it. See this [document](https://scalameta.org/scalafmt/docs/installation.html#intellij) on how to setup it.

## Domain Concepts

After reading the project statement, it is possible to understand that the problem domain based on the scheduling of MSc Disserations Defenses, also known as Vivas. To schedule a Viva, one must take into account its properties, or else two viva schedules may overlap in their realization period. A Viva is held by a student in a specific time duration and addresses a certain topic. This topic is known as the viva title. The viva is assesed by a Jury, that implies the existence of at least two elements: the president of the jury and the adviser. Additonally the jury can also be composed by more elements, the co-advisers and supervisors of the student. Each of these elements is known as a resource that can either be a Teacher or an External. Teachers can take the role of president of the jury, adviser and coadviser, whereas externals can take the role of coadviser and supervisor. A resource is uniquely identifier by a string identifier and is known by a name. A resource is also conducted by a set of availability groups, which identify that the resource is available in a certain time slot. The resource can also give a preference for its availabilities. As seen in the domain model diagram below, the relationship between the jury as as whole and his elements, was represented by a composition of Resource instances, where each of instance is an abstraction of Teacher and External and has a set of roles. One could also depict this relationship as Jury is constituted by a President, Adviser, set of Co Advisers and Supervisors, without needing the abstraction of Resource, but that could lead to problems such as duplication of availabilities for each resource and the possibility of existing two vivas in which the resource that is identified as the jury president in viva X would be a supervisor in viva Y.

Once the vivas are defined, the algorithm can now be applied in order to schedule these into new instances of vivas. These new vivas also have an additional property that is the sum of the availability preferences in which the resources are available at the viva realization period. The complete schedule of the vivas is also known as Agenda.

![domain_diagram](documentation/diagrams/domain.png)

<center>Figure 1 - Domain Model represented in an UML Diagram</center>

## Vivas Scheduling Algorithm (MS01)

## Domain Correctness Assurance with Property-Based Testing (MS02)

## Scheduling Algorithm Refinement with Resource Availability Preference Optimization (MS03)

### Team Members

`Gustavo Moreira 1130604@isep.ipp.pt`

`João Freitas 1160907@isep.ipp.pt`