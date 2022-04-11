# NoctoriBot
Discord Bot made in Java 17 with JDA v5, for Noctori Discord Server.

On load a list will be set up, which will hold identical member objects except with custom variables, for example Noctori Bucks.
When a member gets pushed off the list because it is too big, it will be saved to a file.
Every member on the list will also be saved to a file on shutdown.
This whole list object will be used to keep the memory usage down.

The Managers will be static and not require an object.
Such as Casino, Auto Voice, etc.