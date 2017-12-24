//
// NodeJS global stubs
//

/**
 * To require modules. See the {module}. require isn't actually a global but rather local to each module.
 */
function require(module)
{
}

/**
 * Run callback cb repeatedly every ms milliseconds.
 * Note that the actual interval may vary, depending on external factors like OS timer granularity and system load.
 * It's never less than ms but it may be longer.
 * The interval must be in the range of 1-2,147,483,647 inclusive. If the value is outside that range, it's changed to 1 millisecond.
 * Broadly speaking, a timer cannot span more than 24.8 days.
 * Returns an opaque value that represents the timer.
 */
function setInterval(cb, ms)
{
}

/**
 * Stop a timer that was previously created with setInterval(). The callback will not execute.
 */
function clearInterval(t)
{
}

/**
 * Run callback cb after at least ms milliseconds.
 * The actual delay depends on external factors like OS timer granularity and system load.
 * The timeout must be in the range of 1-2,147,483,647 inclusive.
 * If the value is outside that range, it's changed to 1 millisecond.
 * Broadly speaking, a timer cannot span more than 24.8 days.
 * Returns an opaque value that represents the timer.
 */
function setTimeout(cb, ms)
{
}

/**
 * Stop a timer that was previously created with setTimeout(). The callback will not execute.
 */
function clearTimeout(t)
{
}

/**
 * The name of the directory that the currently executing script resides in.
 */
var __dirname = "";

/**
 * The filename of the code being executed.
 * This is the resolved absolute path of this code file.
 * For a main program this is not necessarily the same filename used in the command line.
 * The value inside a module is the path to that module file.
 */
var __filename = "";

var process = {
	/**
	 * The PID of the process
	 */
	pid: 0,

	/**
	 * What platform you're running on: 'darwin', 'freebsd', 'linux', 'sunos' or 'win32'
	 */
	platform: "",

	/**
	 * Number of seconds Node.js has been running
	 */
	uptime: function()
	{
	},

	/**
	 * Note: this function is only available on POSIX platforms (i.e. not Windows, Android)
	 * Sets the supplementary group IDs. This is a privileged operation, meaning you need to be root or have the CAP_SETGID capability.
	 * The list can contain group IDs, group names or both.
	 */
	setgroups: function(groups)
	{
	},

	/**
	 * A compiled-in property that exposes NODE_VERSION
	 */
	version: "",

	/**
	 * A property exposing version strings of Node.js and its dependencies
	 */
	versions: {}
};

/**
 * In browsers, the top-level scope is the global scope.
 * That means that in browsers if you're in the global scope var something will define a global variable.
 * In Node.js this is different. The top-level scope is not the global scope; var something inside an Node.js module will be local to that module.
 */
var global = {};

/**
 * For printing to stdout and stderr.
 * Similar to the console object functions provided by most web browsers, here the output is sent to stdout or stderr.
 * The console functions are synchronous when the destination is a terminal or a file (to avoid lost messages in case of premature exit)
 * and asynchronous when it's a pipe (to avoid blocking for long periods of time).
 */
var console = {
	/**
	 * Prints to stdout with newline
	 */
	log: function(text)
	{
	},

	/**
	 * Prints to stdout with newline
	 */
	info: function(text)
	{
	},

	/**
	 * Prints to stderr with newline
	 */
	error: function(text)
	{
	},

	/**
	 * Prints to stderr with newline
	 */
	warn: function(text)
	{
	},

	/**
	 * Print to stderr 'Trace :', followed by the formatted message and stack trace to the current position.
	 */
	trace: function(text)
	{
	},

	/**
	 * Starts a timer that can be used to compute the duration of an operation.
	 * Timers are identified by a unique name.
	 * Use the same name when you call console.timeEnd() to stop the timer and output the elapsed time in milliseconds.
	 * Timer durations are accurate to the sub-millisecond.
	 */
	time: function(label)
	{
	},

	/**
	 * Stops a timer that was previously started by calling console.time() and prints the result to the console
	 */
	timeEnd: function(label)
	{
	}
};

/**
 * A reference to the module.exports that is shorter to type.
 * See module system documentation for details on when to use exports and when to use module.exports.
 * exports isn't actually a global but rather local to each module.
 */
var exports = {};

/**
 * A reference to the current module. In particular module.exports is used for defining what a module exports and makes available through require().
 * module isn't actually a global but rather local to each module.
 */
var module = {
	/**
	 * The identifier for the module. Typically this is the fully resolved filename.
	 */
	id: "",

	/**
	 * The fully resolved filename to the module.
	 */
	filename: "",

	/**
	 * Whether or not the module is done loading, or is in the process of loading.
	 */
	loaded: false,

	/**
	 * The module that first required this one.
	 */
	parent: {},

	/**
	 * The module objects required by this one.
	 */
	children: [],

	/**
	 * The module.exports object is created by the Module system.
	 * Sometimes this is not acceptable; many want their module to be an instance of some class.
	 * To do this, assign the desired export object to module.exports.
	 * Note that assigning the desired object to exports will simply rebind the local exports variable, which is probably not what you want to do.
	 */
	exports: exports
};
