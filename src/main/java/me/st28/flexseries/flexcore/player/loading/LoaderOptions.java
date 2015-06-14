/**
 * FlexCore - Licensed under the MIT License (MIT)
 *
 * Copyright (c) Stealth2800 <http://stealthyone.com/>
 * Copyright (c) contributors <https://github.com/FlexSeries>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package me.st28.flexseries.flexcore.player.loading;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents custom settings for a {@link PlayerLoader}.
 */
public class LoaderOptions {

    private boolean isAsynchronous = false;
    private boolean isRequired = false;

    private final List<String> dependencies = new ArrayList<>();

    /**
     * @return true if this loader can run asynchronously.
     */
    public boolean isAsynchronous() {
        return isAsynchronous;
    }

    public LoaderOptions setAsynchronous(boolean isAsynchronous) {
        this.isAsynchronous = isAsynchronous;
        return this;
    }

    /**
     * @return true if this loader must successfully complete for the player to be allowed on the server.
     */
    public boolean isRequired() {
        return isRequired;
    }

    public LoaderOptions setRequired(boolean isRequired) {
        this.isRequired = isRequired;
        return this;
    }

    /**
     * @return a list of other modules that this loader requires.
     */
    public List<String> getDependencies() {
        return dependencies;
    }

    public LoaderOptions addDependencies(String... dependencies) {
        Collections.addAll(this.dependencies, dependencies);
        return this;
    }

}