package com.asiainfo.bits.core.redis;

/**
 * Copyright: Copyright (c) 2013 Asiainfo-Linkage
 *
 * @className: Command
 * @description: Redis命令清单
 * @version: v1.0.0
 * @author: zhoulin2
 * @date: 2013-8-5
 */
public enum Command {

    /**
     * Keys
     */
    DEL, // Delete a key
    KEYS, // Find all keys matching the given pattern
    RENAMENX, // Rename a key, only if the new key does not exist
    EXISTS, // Determine if a key exists
    MOVE, // Move a key to another database
    SORT, // Sort the elements in a list, set or sorted set
    EXPIRE, // Set a key's time to live in seconds
    RANDOMKEY, // Return a random key from the keyspace
    TTL, // Get the time to live for a key
    EXPIREAT, // Set the expiration for a key as a UNIX timestamp
    PERSIST, // Remove the expiration from a key
    RENAME, // Rename a key
    TYPE, // Determine the type stored at key

    /**
     * Strings
     */
    APPEND, // Append a value to a key
    GETBIT, // Returns the bit value at offset in the string value stored at key
    MGET, // Get the values of all the given keys
    SETEX, // Set the value and expiration of a key
    GETRANGE, // Get a substring of the string stored at a key
    MSET, // Set multiple keys to multiple values
    SETNX, // Set the value of a key, only if the key does not exist
    GETSET, // Set the string value of a key and return its old value
    MSETNX, // Set multiple keys to multiple values, only if none of the keys exist
    SETRANGE, // Overwrite part of a string at key starting at the specified offset
    DECR, // Decrement the integer value of a key by one
    INCR, // Increment the integer value of a key by one
    STRLEN, // Get the length of the value stored in a key
    DECRBY, // Decrement the integer value of a key by the given number
    INCRBY, // Increment the integer value of a key by the given amount
    SET, // Set the string value of a key
    GET, // Get the value of a key
    SETBIT, // Sets or clears the bit at offset in the string value stored at key
    BITCOUNT, // Count the number of set bits (population counting) in a string

    /**
     * Hashes
     */
    HDEL, // Delete one or more hash fields
    HINCRBY, // Increment the integer value of a hash field by the given number
    HMGET, // Get the values of all the given hash fields
    HVALS, // Get all the values in a hash
    HEXISTS, // Determine if a hash field exists
    HMSET, // Set multiple hash fields to multiple values
    HGET, // Get the value of a hash field
    HKEYS, // Get all the fields in a hash
    HSET, // Set the string value of a hash field
    HGETALL, // Get all the fields and values in a hash
    HLEN, // Get the number of fields in a hash
    HSETNX, // Set the value of a hash field, only if the field does not exist

    /**
     * Lists
     */
    BLPOP, // Remove and get the first element in a list, or block until one is available
    LLEN, // Get the length of a list
    LREM, // Remove elements from a list
    RPUSH, // Append one or multiple values to a list
    BRPOP, // Remove and get the last element in a list, or block until one is available
    LPOP, // Remove and get the first element in a list
    LSET, // Set the value of an element in a list by its index
    RPUSHX, // Append a value to a list, only if the list exists
    BRPOPLPUSH, // Pop a value from a list, push it to another list and return it; or block until one is available
    LPUSH, // Prepend one or multiple values to a list
    LTRIM, // Trim a list to the specified range
    LINDEX, // Get an element from a list by its index
    LPUSHX, // Prepend a value to a list, only if the list exists
    RPOP, // Remove and get the last element in a list
    LINSERT, // Insert an element before or after another element in a list
    LRANGE, // Get a range of elements from a list
    RPOPLPUSH, // Remove the last element in a list, append it to another list and return it

    /**
     * Sets
     */
    SADD, // Add one or more members to a set
    SINTER, // Intersect multiple sets
    SMOVE, // Move a member from one set to another
    SUNION, // Add multiple sets
    SCARD, // Get the number of members in a set
    SINTERSTORE, // Intersect multiple sets and store the resulting set in a key
    SPOP, // Remove and return a random member from a set
    SUNIONSTORE, // Add multiple sets and store the resulting set in a key
    SDIFF, // Subtract multiple sets
    SISMEMBER, // Determine if a given value is a member of a set
    SRANDMEMBER, // Get one or multiple random members from a set
    SDIFFSTORE, // Subtract multiple sets and store the resulting set in a key
    SMEMBERS, // Get all the members in a set
    SREM, // Remove one or more members from a set

    /**
     * Sorted Sets
     */
    ZADD, // Add one or more members to a sorted set, or update its score if it already exists
    ZINTERSTORE, // Intersect multiple sorted sets and store the resulting sorted set in a new key
    ZREM, // Remove one or more members from a sorted set
    ZREVRANGEBYSCORE, // Return a range of members in a sorted set, by score, with scores ordered from high to low
    ZCARD, // Get the number of members in a sorted set
    ZRANGE, // Return a range of members in a sorted set, by index
    ZREMRANGEBYRANK, // Remove all members in a sorted set within the given indexes
    ZREVRANK, // Determine the index of a member in a sorted set, with scores ordered from high to low
    ZCOUNT, // Count the members in a sorted set with scores within the given values
    ZRANGEBYSCORE, // Return a range of members in a sorted set, by score
    ZREMRANGEBYSCORE, // Remove all members in a sorted set within the given scores
    ZSCORE, // Get the score associated with the given member in a sorted set
    ZINCRBY, // Increment the score of a member in a sorted set
    ZRANK, // Determine the index of a member in a sorted set
    ZREVRANGE, // Return a range of members in a sorted set, by index, with scores ordered from high to low
    ZUNIONSTORE, // Add multiple sorted sets and store the resulting sorted set in a new key

    /**
     * Pub/Sub
     */
    PSUBSCRIBE, // Listen for messages published to channels matching the given patterns
    PUBLISH, // Post a message to a channel
    SUBSCRIBE, // Listen for messages published to the given channels
    PUNSUBSCRIBE, // Stop listening for messages posted to channels matching the given patterns
    UNSUBSCRIBE, // Stop listening for messages posted to the given channels

    /**
     * Transaction
     */
    DISCARD, // Discard all commands issued after MULTI
    MULTI, // Mark the start of a transaction block
    WATCH, // Watch the given keys to determine execution of the MULTI/EXEC block
    EXEC, // Execute all commands issued after MULTI
    UNWATCH, // Forget about all watched keys

    /** Scripting */


    /**
     * Connection
     */
    AUTH, // Authenticate to the server
    PING, // Ping the server
    SELECT, // Change the selected database for the current connection
    ECHO, // Echo the given string
    QUIT, // Close the connection


    /**
     * Server
     */
    BGREWRITEAOF, //
    CONFIG, //
    SAVE, //
    BGSAVE, //
    FLUSHALL, //
    SHUTDOWN, //
    FLUSHDB, //
    RENAMEX, //
    DBSIZE, //
    SUBSTR, //
    LASTSAVE, //
    INFO, //
    MONITOR, //
    SLAVEOF, //
    SYNC, //
    DEBUG; //

    public final byte[] raw;

    private Command() {
        this.raw = this.name().getBytes();
    }
}
