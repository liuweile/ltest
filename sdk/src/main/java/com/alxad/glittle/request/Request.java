package com.alxad.glittle.request;

public interface Request {
    /**
     * Starts an asynchronous load.
     */
    void begin();

    /**
     * Prevents any bitmaps being loaded from previous requests, releases any resources held by this
     * request, displays the current placeholder if one was provided, and marks the request as having
     * been cancelled.
     */
    void clear();

    /**
     * Similar to {@link #clear} for in progress requests (or portions of a request), but does nothing
     * if the request is already complete.
     *
     * <p>Unlike {@link #clear()}, this method allows implementations to act differently on subparts
     * of a request. For example if a Request has both a thumbnail and a primary request and the
     * thumbnail portion of the request is complete, this method allows only the primary portion of
     * the request to be paused without clearing the previously completed thumbnail portion.
     */
    void pause();

    /**
     * Returns true if this request is running and has not completed or failed.
     */
    boolean isRunning();

    /**
     * Returns true if the request has completed successfully.
     */
    boolean isComplete();

    /**
     * Returns true if the request has been cleared.
     */
    boolean isCleared();

    /**
     * Returns true if a resource is set, even if the request is not yet complete or the primary
     * request has failed.
     */
    boolean isAnyResourceSet();

    boolean isEquivalentTo(Request other);

}
