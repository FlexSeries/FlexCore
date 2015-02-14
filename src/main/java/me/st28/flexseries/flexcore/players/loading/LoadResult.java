package me.st28.flexseries.flexcore.players.loading;

public final class LoadResult {

    boolean isSuccess;
    String failMessage;

    public LoadResult(boolean isSuccess, String failMessage) {
        this.isSuccess = isSuccess;
        this.failMessage = failMessage;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public String getFailMessage() {
        return failMessage;
    }

}