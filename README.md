# JavaCommandServer
A tiny web server for executing commands with http post requests.

## Introduction

This little project was done as a workaround for a PHP bug, until i found a more compact solution.
It can be used to execute commands implemented as subclasses of


it.mbcraft.command_server.engine.AbstractCommand


or implementing the interface


it.mbcraft.command_server.engine.ICommand



The command classes can be placed in their own package/subpackages sorted
in a tree-like structure.
Launching the jar with all the required libraries inside the classpath
without parameters will show a brief help.
The server is always bound to localhost, at the default port 8081.
The default command root is 


it.mbcraft.command_server.commands


this means that all the commands (except 'core commands') are searched starting from this package.
This behaviour can be changed using startup parameters (see help).
If a command is not found, a fallback search is done inside the package


it.mbcraft.command_server.engine.core_commands 


All commands are then reachable starting from the root package using a tree-like
url, eg:


POST /image/pdf/merge


Will search for the command : it.mbcraft.command_server.commands.image.pdf.MergeCommand class.

All mandatory command parameters must be sended inside the POST request as form parameters.
Parameters are validated before command execution. 
The command execution log is sent back as a response (plain text or whatever)
with a HTTP 200 OK status code. If any error happens, an appropriate HTTP error
code will be chosen, and error log will be sent back as a response.



-Marco B.

