package kotnexlib

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.yield
import kotlinx.serialization.Serializable
import kotnexlib.UniversalServerStorage.getWithIndexFilter

/**
 * This class is a simple implementation of the API for the UniversalServerStorage.
 * The project is currently still under development and is not (yet) publicly available.
 *
 * This default implementation supports a single API-Key and requires basic-auth!
 */
object UniversalServerStorage {

    private val client by lazy {
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json()
            }

            install(Auth) {
                basic {
                    credentials {
                        BasicAuthCredentials(username = basicUsername, password = basicPassword)
                    }
                    sendWithoutRequest { true }
                }
            }
        }
    }

    /**
     * By convention, this will always be converted to base64. This is done by the library. Do not do it on your own.
     */
    private lateinit var uniqueId: String
    private lateinit var baseUrl: String
    private lateinit var apiKey: String
    private lateinit var basicUsername: String
    private lateinit var basicPassword: String

    /**
     * Init with this before doing the first request! Otherwise the Request will crash.
     * Calling the init multiple times won't work for all variables, like [UniversalServerStorage.basicUsername] and [UniversalServerStorage.basicPassword]
     */
    fun init(uniqueId: String, baseUrl: String, apiKey: String, basicUsername: String, basicPassword: String) {
        this.baseUrl = baseUrl
        this.apiKey = apiKey
        this.uniqueId = uniqueId.toBase64()
        this.basicUsername = basicUsername
        this.basicPassword = basicPassword
    }

    /**
     * Retrieves a single data item from the service based on the provided unique identifier.
     *
     * HTTP Method: GET
     * Endpoint: /service?name={uniqueId}&id={id}
     *
     * @param id The unique identifier of the data item to retrieve.
     * @return A [ResultOf2] object containing either a [Success] with the retrieved [Data] or a [Failure] with an [Error].
     */
    suspend fun getSingle(id: Long): ResultOf2<Data, Error> = try {
        val result = client.get("$baseUrl/service?name=$uniqueId&id=$id") {
            header("API-KEY", apiKey)
        }

        if (result.status.isSuccess()) {
            ResultOf2.Success(result.body<Data>())
        } else {
            result.defaultErrorHandling2()
        }
    } catch (e: Exception) {
        yield()
        ResultOf2.Failure(Error.UnknownError(e))
    }

    /**
     * Fetches multiple `Data` objects based on the provided list of IDs.
     *
     * This function communicates with an external service to retrieve the data associated with the given IDs.
     * It returns a `ResultOf2` object that wraps either the successfully fetched data as a list or an error describing the failure.
     *
     * HTTP Method: GET
     * Endpoint: /service?name={uniqueId}&id={comma-separated-ids}
     *
     * @param ids A list of unique IDs representing the data to fetch.
     * @return A `ResultOf2` object which will be one of the following:
     * - `ResultOf2.Success`: Contains a list of `Data` objects if the operation is successful.
     * - `ResultOf2.Failure`: Contains an `Error` object detailing why the fetch operation failed.
     */
    suspend fun getMulti(ids: List<Long>): ResultOf2<List<Data>, Error> = try {
        val result = client.get("$baseUrl/service?name=$uniqueId&id=${ids.joinToString(",")}") {
            header("API-KEY", apiKey)
        }

        if (result.status.isSuccess()) {
            ResultOf2.Success(result.body<List<Data>>())
        } else {
            result.defaultErrorHandling2()
        }
    } catch (e: Exception) {
        yield()
        ResultOf2.Failure(Error.UnknownError(e))
    }

    /**
     * Retrieves all data items.
     *
     * HTTP Method: GET
     * Endpoint: /service?name={uniqueId}
     *
     * @return A [ResultOf2] object containing either a [Success] with all retrieved [Data] items or a [Failure] with an [Error].
     */
    suspend fun getAll(): ResultOf2<List<Data>, Error> = try {
        val result = client.get("$baseUrl/service?name=$uniqueId") {
            header("API-KEY", apiKey)
        }

        if (result.status.isSuccess()) {
            ResultOf2.Success(result.body<List<Data>>())
        } else {
            result.defaultErrorHandling2()
        }
    } catch (e: Exception) {
        yield()
        ResultOf2.Failure(Error.UnknownError(e))
    }

    /**
     * Identical to [getWithIndexFilter]. But if all parameters are null, an empty list will be returned!
     *
     * HTTP Method: GET (if parameters provided)
     * Endpoint: /service/query?name={uniqueId}&{indexParameters}
     */
    @Deprecated("Use new query method.")
    suspend fun getWithIndexFilterSave(
        indexString1: String? = null,
        indexString2: String? = null,
        indexLong1: Long? = null,
        indexLong2: Long? = null
    ): ResultOf2<List<Data>, Error> {
        return if (indexString1 == null && indexString2 == null && indexLong1 == null && indexLong2 == null) ResultOf2.Success(
            emptyList()
        )
        else getWithIndexFilter(indexString1, indexString2, indexLong1, indexLong2)
    }

    /**
     * Query all data with the given index-datas.
     *
     * HTTP Method: GET
     * Endpoint: /service/query?name={uniqueId}&{indexParameters}
     *
     * If all parameters are null, the whole table content will be returned! Be patient with that!
     */
    @Deprecated("Use new query method.")
    suspend fun getWithIndexFilter(
        indexString1: String? = null,
        indexString2: String? = null,
        indexLong1: Long? = null,
        indexLong2: Long? = null
    ): ResultOf2<List<Data>, Error> {
        return try {
            var queries = ""
            if (indexString1 != null) queries += "&indexString1=$indexString1"
            if (indexString2 != null) queries += "&indexString2=$indexString2"
            if (indexLong1 != null) queries += "&indexLong1=$indexLong1"
            if (indexLong2 != null) queries += "&indexLong2=$indexLong2"
            val result = client.get("$baseUrl/service/query?name=$uniqueId${queries}") {
                header("API-KEY", apiKey)
            }

            if (result.status.isSuccess()) {
                ResultOf2.Success(result.body<List<Data>>())
            } else {
                result.defaultErrorHandling2()
            }
        } catch (e: Exception) {
            yield()
            ResultOf2.Failure(Error.UnknownError(e))
        }
    }

    /**
     * Sends a query request to the service and retrieves the filtered data as a result.
     *
     * The method uses the provided query builder to construct the query data,
     * sends it to the configured service endpoint, and processes the response.
     *
     * @param queryBuilder A lambda function that generates a list of `QueryData` objects
     *                     representing the query criteria to be sent to the service. Everything is connected with AND!
     * @return A `ResultOf2` object containing either a list of `Data` on success
     *         or an `Error` on failure.
     */
    suspend fun getData(queryBuilder: () -> List<QueryData>): ResultOf2<List<Data>, Error> {
        return try {
            val result = client.post("$baseUrl/service/query?name=$uniqueId") {
                header("API-KEY", apiKey)
                setBody(queryBuilder())
            }

            if (result.status.isSuccess()) {
                ResultOf2.Success(result.body<List<Data>>())
            } else {
                result.defaultErrorHandling2()
            }
        } catch (e: Exception) {
            yield()
            ResultOf2.Failure(Error.UnknownError(e))
        }
    }

    /**
     * Stores [data] on your server.
     * id == 0 --> New entry within the db
     * id != 0 --> Update existing entry or error, if entry does not exist!
     *
     * @param data The data to be serialized and sent as the request body.
     * @return A [ResultOfEmpty] object that represents either the success or the failure of the operation.
     *         On success, the result will be [ResultOfEmpty.Success]. On failure, the result will be
     *         [ResultOfEmpty.Failure] containing an appropriate [Error] instance.
     */
    suspend fun post(data: Data): ResultOfEmpty<Error> = try {
        val result = client.post("$baseUrl/service") {
            header("API-KEY", apiKey)
            setBody(data)
            contentType(ContentType.Application.Json)
        }

        if (result.status.isSuccess()) {
            ResultOfEmpty.Success
        } else {
            result.defaultErrorHandling()
        }
    } catch (e: Exception) {
        yield()
        ResultOfEmpty.Failure(Error.UnknownError(e))
    }

    /**
     * Deletes a single data item by its ID.
     *
     * HTTP Method: DELETE
     * Endpoint: /service?name={uniqueId}&id={id}
     *
     * @param id The unique identifier of the data item to delete
     * @return A [ResultOfEmpty] object indicating success or containing an [Error] on failure
     */
    suspend fun delete(id: Long): ResultOfEmpty<Error> = try {
        val result = client.delete("$baseUrl/service?name=$uniqueId&id=$id") {
            header("API-KEY", apiKey)
        }

        if (result.status == HttpStatusCode.NoContent) {
            ResultOfEmpty.Success
        } else {
            result.defaultErrorHandling()
        }
    } catch (e: Exception) {
        yield()
        ResultOfEmpty.Failure(Error.UnknownError(e))
    }

    /**
     * Deletes multiple data items by their IDs.
     *
     * HTTP Method: DELETE
     * Endpoint: /service?name={uniqueId}&id={comma-separated-ids}
     *
     * @param ids The list of unique identifiers of the data items to delete
     * @return A [ResultOfEmpty] object indicating success or containing an [Error] on failure
     */
    suspend fun delete(ids: List<Long>): ResultOfEmpty<Error> = try {
        val result = client.delete("$baseUrl/service?name=$uniqueId&id=${ids.joinToString(",")}") {
            header("API-KEY", apiKey)
        }

        if (result.status == HttpStatusCode.NoContent) {
            ResultOfEmpty.Success
        } else {
            result.defaultErrorHandling()
        }
    } catch (e: Exception) {
        yield()
        ResultOfEmpty.Failure(Error.UnknownError(e))
    }

    private suspend fun HttpResponse.defaultErrorHandling() = when (status.value) {
        404 -> ResultOfEmpty.Failure(Error.NotFound)
        401 -> ResultOfEmpty.Failure(Error.Unauthorized(bodyAsText()))
        400 -> ResultOfEmpty.Failure(Error.BadRequest(bodyAsText()))
        else -> ResultOfEmpty.Failure(Error.UnknownError(null))
    }

    private suspend fun HttpResponse.defaultErrorHandling2() = when (status.value) {
        404 -> ResultOf2.Failure(Error.NotFound)
        401 -> ResultOf2.Failure(Error.Unauthorized(bodyAsText()))
        400 -> ResultOf2.Failure(Error.BadRequest(bodyAsText()))
        else -> ResultOf2.Failure(Error.UnknownError(null))
    }


    /**
     * Data model class representing a stored item in the database.
     *
     * This class is used for both database persistence (via ObjectBox @Entity)
     * and API serialization (via kotlinx.serialization @Serializable).
     *
     * @property id The database primary key, automatically assigned by ObjectBox if not provided. Use an existing one to update an existing item.
     * @property uniqueId A unique identifier string for the data item. Must be at least 10 characters long.
     *                   This is used as the collection name in the database.
     * @property version Version number of the data, defaults to 1
     * @property timestamp Creation or last update time in milliseconds since epoch.
     *                    Automatically set to current time if not provided.
     * @property indexString1 Universal string index field for data querying purposes
     * @property indexString2 Universal string index field for data querying purposes
     * @property indexLong1 Universal long index field for data querying purposes
     * @property indexLong2 Universal long index field for data querying purposes
     *
     * @property rawData The actual data content stored as a string. Must not be blank.
     */
    @Serializable
    data class Data(
        val id: Long = 0,
        val uniqueId: String = UniversalServerStorage.uniqueId,
        val version: Int = 1,
        val timestamp: Long = System.currentTimeMillis(),
        val indexString1: String? = null,
        val indexString2: String? = null,
        val indexLong1: Long? = null,
        val indexLong2: Long? = null,
        val rawData: String
    )

    sealed class Error(val msg: String?) {
        class UnknownError(val e: Exception?, msg: String? = null) : Error(msg)
        data object NotFound : Error(null)
        class Unauthorized(msg: String?) : Error(msg)
        class BadRequest(msg: String?) : Error(msg)
        class InternalServerError(msg: String?) : Error(msg)
    }

    /**
     * Represents a sealed class for querying data based on different criteria or attributes.
     * Each subclass specifies the type of query performed and the associated query type.
     */
    @Serializable
    sealed class QueryData() {
        @Serializable
        data class Version(val queryNumberType: QueryNumberType) : QueryData()

        @Serializable
        data class Timestamp(val queryNumberType: QueryNumberType) : QueryData()

        @Serializable
        data class IndexLong1(val queryNumberType: QueryNumberType) : QueryData()

        @Serializable
        data class IndexLong2(val queryNumberType: QueryNumberType) : QueryData()

        @Serializable
        data class IndexString1(val queryStringType: QueryStringType) : QueryData()

        @Serializable
        data class IndexString2(val queryStringType: QueryStringType) : QueryData()
    }

    /**
     * Represents a type of query string comparison. It serves as a base sealed class
     * that defines different kinds of operations that can be performed on query strings.
     */
    @Serializable
    sealed class QueryStringType() {
        @Serializable
        class Equal(val otherString: String) : QueryStringType()

        @Serializable
        class EqualIgnoreCase(val otherString: String) : QueryStringType()

        @Serializable
        class Contains(val otherString: String) : QueryStringType()

        @Serializable
        class ContainsIgnoreCase(val otherString: String) : QueryStringType()

        @Serializable
        data object Null : QueryStringType()
    }

    /**
     * A sealed class representing various types of numeric queries.
     * Each subclass defines specific conditions for comparing numbers.
     */
    @Serializable
    sealed class QueryNumberType() {
        @Serializable
        class Equal(val otherNumber: Long) : QueryNumberType()

        @Serializable
        class Higher(val otherNumber: Long) : QueryNumberType()

        @Serializable
        class Lower(val otherNumber: Long) : QueryNumberType()

        @Serializable
        class EqualHigher(val otherNumber: Long) : QueryNumberType()

        @Serializable
        class EqualLower(val otherNumber: Long) : QueryNumberType()

        /**
         * Between [lower] and [higher] inklusive
         */
        @Serializable
        class Between(val lower: Long, val higher: Long) : QueryNumberType()

        @Serializable
        data object Null : QueryNumberType()
    }

}

