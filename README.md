# Invoke Oracle Functions by Id using the OCI Java SDK

This example demonstrates how to invoke a function deployed to Oracle Functions
by its id using (a preview version of) the Oracle Cloud Infrastructure Java SDK.

The OCI Java SDK exposes two endpoints specificially for Oracle Functions:

- `FunctionsManagementClient` - which provides APIs for function and application
  lifecycle management
- `FunctionsInvokeClient` - for invoking functions

The SDK also provides a number of utility classes for building function
invocation requests and handling request results.

In this example, we're invoking a existing function directly by its id so we
will only need the `FunctionsInvokeClient`.  The key method we're going to use
is the suitably named `invokeFunction`, which takes an `InvokeFunctionRequest`.

The set of required steps are:

1. Authenticate with OCI (more on this below)
2. Create a `FunctionsInvokeClient` with the auth credentials
3. Set the `invokeEndpoint` of the client to the URL of the Oracle Functions service in your region
3. Create an `InvokeFunctionRequest` with the function id
4. Pass the `InvokeFunctionRequest` to the `FunctionsInvokeClient`to call the function
5. Extract the result from an `InvokeFunctionResponse`


## Pre-requisites

1. Install/update the Fn CLI

   `curl -LSs https://raw.githubusercontent.com/fnproject/cli/master/install |
   sh`

2. Create a function to invoke

   Create a function using [Go Hello World
   Function](https://github.com/abhirockzz/oracle-functions-hello-worlds/blob/master/golang-hello-world.md)

## Install preview OCI Java SDK

As this example uses Maven, you need to install the OCI SDK JAR to your local
Maven repository:

1. Download and unzip the preview version of the OCI Java SDK

   `unzip oci-java-sdk-dist-1.4.1-preview1-20190222.223049-5.zip`

2. Change into the correct directory

   `cd oci-java-sdk-dist-1.4.1-preview1-20190222.223049-5`

3. Install the JAR to local Maven repo

   `mvn install:install-file -Dfile=lib/oci-java-sdk-full-1.4.1-preview1-SNAPSHOT.jar
   -DgroupId=com.oracle.oci.sdk -DartifactId=oci-java-sdk \
   -Dversion=1.4.1-preview1-20190222.223049-5 -Dpackaging=jar`

## Build the JAR and configure your environment

1. Clone this repository

   `git clone https://github.com/shaunsmith/fn-java-sdk-invokebyid`

2. Cd into the directory where you cloned the example: 

   `cd fn-java-sdk-invokebyid`

3. Then build the JAR:

   `mvn clean package`

4. Define OCI authentication properties

    Functions clients need to authenticate with OCI before being able to make
    service calls. There are a few ways to authenticate. This example uses an
    `ConfigFileAuthenticationDetailsProvider`, which reads user properties from
    the OCI config file located in `~/.oci/config`. This class can be instructed
    to read an optionally specified user profile.  But out of the box, the
    example uses the DEFAULT profile.

    If you've already set up the Fn CLI to use Oracle Functions, as described in
    the *Oracle Functions Getting Started* guide, then you will have already
    defined a profile. If it's not called `DEFAULT` then you can edit the
    example to set the `PROFILE_NAME` constant to point to your profile.

    You should have something like the following in your OCI `config` file:

   ```shell
   [DEFAULT]
   region=us-phoenix-1
   tenancy=<OCID of your tenancy>
   user=<OCID of the OCI user>
   fingerprint=<public key fingerprint>
   key_file=<location of the private key on your machine>
   pass_phrase=<keyfile pass phrase>
   ```

   > NOTE: `pass_phrase` is only required if your private key has a passphrase

## You can now invoke your function!

The Maven build results in a jar in the target folder.  The syntax to run the
example is:

`java -jar target/<jar-name>.jar <invoke endpoint> <functionid> [<payload string>]`

To find the Orace Functions invoke endpoint for your function, inspect the
function you want to invoke using the Fn CLI, e.g.,: `fn inspect sdktest
helloj`. The result will be a JSON structure similar to the following:

```JSON
{
	"annotations": {
		"fnproject.io/fn/invokeEndpoint": "https://toyh4yqssuq.us-phoenix-1.functions.oci.oraclecloud.com/invoke/ocid1.fnfunc.oc1.phx.abcdefghijk",
		"oracle.com/oci/compartmentId": "ocid1.compartment.oc1..abcdefg"
	},
	"app_id": "ocid1.fnapp.oc1.phx.abcdfg",
	"created_at": "2019-02-26T21:28:04.866Z",
	"id": "ocid1.fnfunc.oc1.phx.abcdefghijk",
	"idle_timeout": 30,
	"image": "phx.ocir.io/oracle-serverless-devrel/shaunsmith/helloj:0.0.4",
	"memory": 128,
	"name": "helloj",
	"timeout": 30,
	"updated_at": "2019-02-26T21:28:04.866Z"
}
```

The invoke endpoint you need to pass to the example can be extracted from the value of the `fnproject.io/fn/invokeEndpoint` property.  You just need the protcol and name of the host.  For the example above that would be: `https://toyh4yqssuq.us-phoenix-1.functions.oci.oraclecloud.com`.  The `id` property contains the function id.

> NOTE: Payload is optional. If your function doesn't expect any input you
> can omit it.

e.g., without payload:

`java -jar target/fn-java-sdk-invokebyid-1.0-SNAPSHOT.jar https://toyh4yqssuq.us-phoenix-1.functions.oci.oraclecloud.com ocid1.fnfunc.oc1.phx.abcdefghijk`

e.g., with payload:

`java -jar target/fn-java-sdk-invokebyid-1.0-SNAPSHOT.jar https://toyh4yqssuq.us-phoenix-1.functions.oci.oraclecloud.com ocid1.fnfunc.oc1.phx.abcdefghijk '{"name":"foobar"}'`

## What if my function needs input in binary form?

See the [Invoke by Function name](https://github.com/abhirockzz/fn-java-sdk-invoke) example for details on
how to attach a binary payload to an `InvokeFunctionRequest`.

## Troubleshooting

1. If you fail to provide a DEFAULT profile in the OCI `config` file you'll get
   the following exception:

   Exception in thread "main" java.lang.NullPointerException: missing fingerprint in config

2. If you provide an invalid value for function id you'll get an exception
   similar to the following:

   Exception in thread "main" com.oracle.bmc.model.BmcException: (404, Unknown, false) Unexpected Content-Type: application/json;charset=utf-8 instead of application/json. Response body: {"code":"NotAuthorizedOrNotFound","message":"Resource is not authorized or not found"} (opc-request-id: F8BC6E9DC19F44BD8E8967AAEC/01D5424B1F1BT1AW8ZJ0003Z6C/01D5424B1F1BT1AW8ZJ0003Z6D)

3. If you provide an incorrect `tenancy` or `user` or
   `fingerprint` in your OCI `config` file you'll receive an authentication exception similar to the following:

   Exception in thread "main" com.oracle.bmc.model.BmcException: (401, Unknown, false) Unexpected Content-Type: application/json;charset=utf-8 instead of application/json. Response body: {"code":"NotAuthenticated","message":"Not authenticated"} (opc-request-id: 3FD3E66DF81F4BB490A6424530/01D5427GTX1BT1D68ZJ0003Z9E/01D5427GTX1BT1D68ZJ0003Z9F)
