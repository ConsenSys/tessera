package com.quorum.tessera.data.migration;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class DirectoryStoreFileTest {

    private DirectoryStoreFile loader;

    @Before
    public void init() {
        this.loader = new DirectoryStoreFile();
    }

    @Test
    public void loadOnNondirectory() throws IOException {
        final Path randomFile = Files.createTempFile("other", ".txt");

        final Throwable throwable = catchThrowable(() -> loader.load(randomFile));
        assertThat(throwable)
                .isNotNull()
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("doesn't exist or is not a directory");
    }

    @Test
    public void loadOnNonexistentFolder() throws IOException {
        final Path randomFile = Files.createTempFile("other", ".txt").getParent().resolve("unknown/other").getParent();

        final Throwable throwable = catchThrowable(() -> loader.load(randomFile));
        assertThat(throwable)
                .isNotNull()
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("doesn't exist or is not a directory");
    }

    @Test
    public void loadProperDatabaseHasNoError() throws URISyntaxException {
        final Path directory = Paths.get(getClass().getResource("/dir/").toURI());

        final Throwable throwable = catchThrowable(() -> loader.load(directory));

        assertThat(throwable).isNull();
    }

    @Test
    public void nextReturnsEntryWhenResultsAreLeft() throws URISyntaxException, IOException {
        final Path directory = Paths.get(getClass().getResource("/dir/").toURI());

        this.loader.load(directory);

        // There should be 22 results left in the database
        final DataEntry next = this.loader.nextEntry();

        assertThat(next).isNotNull();
    }

    @Test
    public void hasNextReturnsFalseWhenNoResultsAreLeft() throws IOException, URISyntaxException {
        final Path directory = Paths.get(getClass().getResource("/dir/").toURI());

        this.loader.load(directory);

        for (int i = 0; i < 22; i++) {
            final DataEntry next = this.loader.nextEntry();
            assertThat(next).isNotNull();
        }

        // There should be 0 results left in the database
        final DataEntry next = this.loader.nextEntry();

        assertThat(next).isNull();
    }

    @Test
    public void dataIsReadCorrectly() throws IOException, URISyntaxException {
        final Path directory = Paths.get(getClass().getResource("/dir/").toURI());

        final Map<String, String> expectedResults = new HashMap<>();
        expectedResults.put(
                "mzdv8f+0C3SIuIf5EyIwl35mTc/13KPSNPBV4ODoCooALb/Kn96VUrOOfYd91neyaWL1vwLgv0nNUdAaUfKCLA==",
                "AAAAAAAAACAFQt5HwnJRaGK64IxT8csDRDmnORhP5wcgjdkoF7LcGgAAAAAAAAF561PwTQxwrntGnvM7qfbyIYK5V7U2uAgO5kgUjzAuqQMXP+J60W7KBD2YFZFI+GGMPjJbcWbWNnB8TQeB3aC9mtB/U7qciy7Qz9uQPKwcYRwO1gzVjzMAV1vYbXdRQvvyEpyguwdtEL+WpT8hVSvTi0yVFzntiM8d76JbUuhKXZ65mM/1oFKxXQAsxOmTih8M2oJdTQ3vLysRb21A8fI6OCvBd0jqgt/ni36/mtPeAZEGSDchW41Qyc2I9gE2LOFpKbq6xfHeydKdxDjhg29LlR7VlaqICdyFTFimnGlKgf2ymE9NQukOKQwQ7Yli6a0JEGUdKl8neFA6zSQciLrk1AMOKJX2F0mPEXtM5nPjam0l2QB4/VVDZB4wOHEaUSOqvj69RRm3YMbCNxNtPjnUsKE3uWWodOcQEJ79n+3ILOpckPMozF7uvLl/Fmb/6uxL/q4bFO8d3MViQqBaSUUY3yeBhDtT7PhFiXb4LilCIUjr8x9VkkkwSqcAAAAAAAAAGE33CfyL12lsGdf79G0zsPKPxhe5pvvveQAAAAAAAAABAAAAAAAAADCywD3qWSYG+L7pwJTijkNAym5dI1FkPRoe3v/VUZ6TzuPS37KrjeWH0IFoUEX3UNoAAAAAAAAAGB0HYxSzebq5pjD5RzA0z2d3qHpSKSmrlQAAAAAAAAABAAAAAAAAACBE4BkFa1JpzFdCs57cUYCokPImMV49Hlx7hNIjOYnQFw==");
        expectedResults.put(
                "0mK7GuWQ00xRQBu/UrtoavX7UuHWBx6USNxJAc0mv4tFoGJDMDrw+tvLQgrFwFk0Um3GiSAVkADKItMBOEKZ8Q==",
                "AAAAAAAAACAFQt5HwnJRaGK64IxT8csDRDmnORhP5wcgjdkoF7LcGgAAAAAAAAF5Gzz2e1GTq/NvkIjdpuvkJCxZaG/dEbi23di7t5vSqRXEL6VM6454Jcff8vRY9iOHGYt2t0jyZDnIoZ/rX4ecC9uvfvgnnbPFmpptIe1L74ny3dqTqRvNPDCSIWVms//1AshfHw1VXiJNt2rxEg8Hch2i9Aj8BZMvhimMYvDBooBpwzVGMlSFuQokkV+rIq7gdzxbAE9h2Qln4UBf2kxergsiEWm4xzVhnKY1xUbU8wJhSQ1ofa4/0GnOPHIWmqPBTEZRIYJ9rR3w4DTSzEOvhHezDeMchi7coKf7VuR83bp0dx9mEFuBTmEqkg37p9OodjKRYKPYS6VBv4sb7t854fuQ3qIVTCbA13xykB9z6e0EJmnHBH9TYhM4b4Tgn8aThbDNRYgCmDRZ5q7vvo06/SR2FA1+C0ucjnpchKq/qWi16mYRiAugLkNETWc6DlB6W/zNEhVf9XQ4/70a1wBygrk6HNCFRP4vADA9UIBGVT8EzUu4TU6rjp4AAAAAAAAAGHys+XkxZYiL4zU8sfH/yHqHJAguBUhlNwAAAAAAAAABAAAAAAAAADBD5fWoge56bvn2tVBMKBoiTQKXhVGpB7L1q8NnC9b/D+WUq12kJDCIUhKwAhWKnl4AAAAAAAAAGCXRDSFKsBwKLQdVkNiqzQpgLXZK7+dkvAAAAAAAAAABAAAAAAAAACBE4BkFa1JpzFdCs57cUYCokPImMV49Hlx7hNIjOYnQFw==");
        expectedResults.put(
                "pQwD2xWsZme7EOydzDD7h9ZV5DhDOvrsxLi8hGKpEC9806MFd2UwpghGiDfDAX2tdvbkvGM9rGjhlAdw80dvmg==",
                "AAAAAAAAACAFQt5HwnJRaGK64IxT8csDRDmnORhP5wcgjdkoF7LcGgAAAAAAAAF5EdODiwpj8PWEIbPnmw29cka8h8KLWbOaFWPDGZWMqSEfJzpml4sUKu2oKHtelhyaJ3s4AlIV9fZf8VR6fHNhRCnUvUI/lDG20Ou4Y4A+nFjaDyhGbkiTWGkcEfJqIViDf5zXzhCABLQz2kPTPjG9n6REz/qwISQGpTXBWYOzhCOj549dYZl5CRj8iFswuhlEYqdGnxarZZMvOCZ1k4HORybC+shRPyyREm8EM0Vaiw3vEnJEwkXkmnFoRTfxPZkqMp4/Z84B/3Ensmpt1f34jp5nvttwGCQuKMojOcgHVbXWtMvXjaunpeDixI4Zenp4ywToD/4eeDfr8Y5Dbo4TyYcM/WTjMSDG8xwIwwpze4sTY9wpGErFuRtBeJfaPMLBfRU0yRrnsrfL+dQnHyLnZzs3wN+oZAUfBjCVO0t1vE7uQlrRUdG5SZ9dAKGVqi6xwGHROyxMSpYGBSXVNah8L+b1oXBlAdujkh5JyJfMMK2c+81WrEHjPWcAAAAAAAAAGEhzlhwK88Zm3aGoB2TxJYOR5OEpBI+0xgAAAAAAAAABAAAAAAAAADAZdUIxSXMoroqJlmlXeJts1+xV3fGvRDRJz/LbOQ9aoYGTisaoy08J4KBN+u5WOScAAAAAAAAAGJddMXM/2xHzLby6zIEIVl5y2gPAb+PaHwAAAAAAAAABAAAAAAAAACBE4BkFa1JpzFdCs57cUYCokPImMV49Hlx7hNIjOYnQFw==");
        expectedResults.put(
                "vVwGQXP6VQWlUZqpiBVbmbsBHYyluc79XZl8j92nj/EhBI0aJUsAEWqoYskx1IF+6fE+vkDspwy/UulJGpOhWw==",
                "AAAAAAAAACAFQt5HwnJRaGK64IxT8csDRDmnORhP5wcgjdkoF7LcGgAAAAAAAAF5fvR1ykK/V6Je52ijerCWmvlKUCQo2RSUFr2wkMrXQVVo9QJGVUQ3MPQhJF6Trj4ivSMe9t0NGxZD+u0eQsKKnTX+2lJ///5Lb4RawuauWub7dYfxAZR+u1f3mV4K0FmQwf8zSdQrPRgw4BI4dYqDjliy19zH1Rrjupay0vb45BjOiLnymlV8pzdnAc9jBpwzQf5XGc8MFoUAC0MbvJP83ymAUDXYFdRZg2yogzCt7DY4Fl/99OZA4vaU7/GuKLFpgVRyJSbkE+XAPqCo1vOzGU0d395IGnaogm04Q8v74saaXIdg5sFzwgOc6doai/5wEe3Ux5w+ccrlwPGM++9BV0bWBGmc4YrMRMX4uqBNA4N6azmhGxfQyR33021ZoNGzsunXUVKDZWgI5ETtWxwvzgQ3ZGsEvAXtyiMzl96ZEllkMUfcNcKruj2LS2hKjyg5bzc2BMfuU56q3+poq3AAweesaVvr1F2mR3BCTfKhwR9DPu2D61ia5TIAAAAAAAAAGCz5T3IPufChVGnrNr5Hg9okK0Xr71rexwAAAAAAAAABAAAAAAAAADBhaJWsUn5w5KmtvQHXmK378AX0nqG1bQ5rX+oe3iXVwUty/xeJvPUwcUIQ4tYj+yYAAAAAAAAAGAeINQCwk5XxXLBWeaWBIW6BG5ufMaLKLwAAAAAAAAABAAAAAAAAACBE4BkFa1JpzFdCs57cUYCokPImMV49Hlx7hNIjOYnQFw==");
        expectedResults.put(
                "0t8/H7tSx6JocXYGmtc0XxqEl6fSYGOTciQw+Xa7fJygqcaRpapteCyWssYFEm7R3xkCq0TtkF95gYxV9Fs6Xg==",
                "AAAAAAAAACAFQt5HwnJRaGK64IxT8csDRDmnORhP5wcgjdkoF7LcGgAAAAAAAAF59wzHs5MJBjHdZT8pq81LypfpvpARc+X7JgB0nTdCDRZ9VmYjpug59ekXnSXw6ktsXbdrKnnsn2TKQHa1kq2bM3Xn1u4K/lBSP2t55WlywX+L+qxPb4kZJ/PfLMogHda6GAvNJ1fLcPBEmgwLSbU/f8Wehb/1fCu50Is+Nwk5gKcAfILpbk1aQxxeKd3hzEpCL5LyOElvwt8DuLaeg0oAaNu8/jwk8ihOybNdgL3Z+PEPdqHUGFTWb7XVKAHbXIQsbo5OuVRASYroIvLbCTPN1qjgB+P0yGxr+u0M8FQ4x8zNldpV4fFeypF2Y3L6ieKsdYmOgYjVW5mm/JZ3Q18iehxTHesXVUrlG1lLzd8SFDZ/0RWeHcsnNBX6wuS/DKhgo5pGAlMj7LT1CYj51HZ7EgOhMXwyGUPHoB2NESMLQ0kpTsPo2srGBPIJ3/R+kTNC+weIsSMoh6bPDLvWBGVBEHgEsws3K6mXMEMAsaLqwEanBT0Jije0j5gAAAAAAAAAGIoHsOiAnu34sMbZlvylMdGyuhUxFxytAwAAAAAAAAABAAAAAAAAADD5DwDpEPEmeBQIfwV/7VenWYhvye4JdJ+7EZ8We5PDkANba0EVnKIkPpuw1pTFvB8AAAAAAAAAGK07/RWzwD0/Ur0ngwJnTiKC6TINfAxzkgAAAAAAAAABAAAAAAAAACBE4BkFa1JpzFdCs57cUYCokPImMV49Hlx7hNIjOYnQFw==");
        expectedResults.put(
                "ERzVH86S45Tf2cRkp4Qmfp5/Z75qcathNuFfxi55tHR6/igfVru1A5Eb32c1qaSL6d4hD+7f5HFdYJVSjlrnQw==",
                "AAAAAAAAACAFQt5HwnJRaGK64IxT8csDRDmnORhP5wcgjdkoF7LcGgAAAAAAAAF5d7zPTBPygNJhPLT/yEFMN2X5K7A7OYuDgThS8eAqG1n79fTmq20j8eW0vSeDr1oN7v89txjB78HEoGeW4wOWRDjxNClBL8IuYvxAC99Tigiy9WWXfGX/8KEnNZ/trAC5lW5h+sKKB5t4knMe8xOlYfA2fUPS7SitB9/JWIjzMh2gmneLw6yUM/JhtITdYnY6LQFOQFBc4a0vdYm6Lbtv2oojgXXSUqACyw90Qds4HPqcFJumvEKOa9V6ZlqOP8M16q3yDpWnR9Wpb+SGbJMVVW/qidCy+Cn9ot2SOgOhkDe8rcj2aLDVJir3nlmr4SV21sGBII2kBCgUOUucTA89Bua7EwwMICBPbYRlJmSdtrub1JdJPDhseV1UFSuH56z8y1f5x/ZBX5XUiW3e+c/QI+7fMPR/ewhQu9sr65/Bf4/sSs8dnD5ctypgBTIUU6RSD3iQbJwyNv7GBdpYXwV+St2x2B1tidyU5Gh5n0jqYlMh9Em3OITJA28AAAAAAAAAGCoPrdRvmez53BLyuJKWxoBhoYRqBlk1pAAAAAAAAAABAAAAAAAAADCDo02JSLOmWT14NI8mTZOXri9X9urrEnQxymYLrnZ0oVnmzPs03mjKwPXRYfWXrIgAAAAAAAAAGJx60IZRHrkcAJ2fKsOPUkj/4QPdzA8FYQAAAAAAAAABAAAAAAAAACBE4BkFa1JpzFdCs57cUYCokPImMV49Hlx7hNIjOYnQFw==");
        expectedResults.put(
                "lMfayeXofJKv0O7Z+xAFpK9ccX6XtJGlenuc0vP64IYS9P4fYPQqxa6SIQuJJfZbVy0w8UMv6Ow/OGBfQ7dgsw==",
                "AAAAAAAAACAFQt5HwnJRaGK64IxT8csDRDmnORhP5wcgjdkoF7LcGgAAAAAAAAF5+MRFK0h3G6W40gT+3y3KGCXu3Keouo395TYJ5dOO9biMl6z7bYinaK3bPEbG1bOgNQvzvcKa2aODy69O5B3cVricH3I7w/Ms1Ij9XdmYwQBcBfKjWoun9bW+oFBFff4MSVfffh8wFHH1TyVhBLf+OPrLhv3ATB5pFjoTpRFKDF1ZDjW1qgSS4sYPWSJXaCMOc/eCx0pB0qMecAFtn16sythwGtI6sXwMZK1N+YLecXMoJrkXFbGmD/aY9Q150vAziWhiVJBcSrcjtEGnPK2bAoCvbwxrTN21QF4aKQmOdgvu9THOp0uVmsc7chDQCljZkaRqD8M7iw6mRcccABwnkoc5gDTg1pxyTvR+X8yv+UEHhynjHRihTV1Dt2oakqFg0ZVveyyX2rlW4p9GazQvD8fIBAl3UIorCmfEDRsxv+AsWqGwPFZy8vS1ampk84vbXuYhZh97318c+9OdyRy2F/SUXOsRm51H0Kco09zY8S4AVnJhezomWvAAAAAAAAAAGEHUXCbn6RWiXTlrJgYdrppt4mn5u/7uTgAAAAAAAAABAAAAAAAAADDa4I1n3wsoMuLxBbB02enu5okwLpG4JHAiEes3MUE54XI9m1xrjF0OH0i8l5qEapIAAAAAAAAAGOU+v7SS5UpJQhbUsCQ2PDNtwpuO46DKRwAAAAAAAAABAAAAAAAAACBE4BkFa1JpzFdCs57cUYCokPImMV49Hlx7hNIjOYnQFw==");
        expectedResults.put(
                "Be5pcmbGTWJlBeKF8/wELqbgm5bLifK92945duOEBwPvR4Yk3qzzLy52wUfpU2mz5eWuXzsbEoRVz6nsPyCluw==",
                "AAAAAAAAACAFQt5HwnJRaGK64IxT8csDRDmnORhP5wcgjdkoF7LcGgAAAAAAAAF52yR0aRdUmwJsCyVJqMVCpD2rR9VG7S/luCy7adcGQv/zm+tdhYHh/fsJppEnEM0EtSQ+aKC4VsTfF0rIXLV05zhtyN/Y97cLPeM2+ld/vIDw3b3GV3E3uIOazHQPfFdsGwuU24hUFCrHFxv4s7z5ALIq4POrJzHpGbkc+YqA7nys6O2Cc1DTZf1ufURw/VA8HbDDe1WNfCmDWTZsXZdCuyXKXa1LkAsyg9j/XA8dPyCpZ07kSPI0qof2Roc3fhYluKMbtco5LcTrGaeaaiPWeT5sSRltS2j9uY6N+CELnZiRHEqMteFgb50VEP78+0kXvirNt8+FUcPHlI/m2dFwq9CfeOLWAnCRM22IZELbMNDtQKuSBvI1ygbDMdmam6wx1kerIRleOTK6ysegipsZ2ZnzdVjwtZWs0zxUhNcH5eb8zl1pk/RY/i/eJ8yu+17lGKQ23IIDWF57tO9uetNugZ+E/NYesdiOlQGfqzzhMtZ1j4BjlxYA96EAAAAAAAAAGMihyEh/7GA2KB/an8bRg90MrbXx5/dl+wAAAAAAAAABAAAAAAAAADDfPwvaHIi7TeFsm5ZwHDM7eWDtobqWTPGuKnhl3Hyr5SoFvizt8A/WuQyBoLVtggEAAAAAAAAAGOwdqxo6nHiwDTDNwh7r+SGVkC2x4Dls8AAAAAAAAAABAAAAAAAAACBE4BkFa1JpzFdCs57cUYCokPImMV49Hlx7hNIjOYnQFw==");
        expectedResults.put(
                "GUYFW8E8eGpqyg34oSqrjl+NlNhSi9rNsDjfjmCMMyPI/l1P3aCsKvjO6Nm+BKyFV/vCOCDlXseSEB7Ezs4TtQ==",
                "AAAAAAAAACAFQt5HwnJRaGK64IxT8csDRDmnORhP5wcgjdkoF7LcGgAAAAAAAAF5e0++7E+VAcsDIecEkJY8bpKmKhkNX6WX7lMMKfGsJzmbUmY+QOGY6TfosmawwB6RSFUXiTOz8lmDE97Ok2txiJ5egb5LkUwVdRtC0P0xyXn7aDJFvHlhoettFWl6a83o36H5pRhoeFEi016gv+XnGIKurIC1Yj8GG+0Yvyp1Fo3bVPvFRGdbvNN/VP3b7LNWsP2UBxYALdrIyG8ls74/Ri0b5H0PPaaBwXsrlA9tWXtC42j4YcVoBy8I8MqmjAG2PGM4tH+JpfNYCgfXXHHhewRqgNBfrsNIUTknyp7qbWAE9bzsb8VQQ7uy0rLZlMDKJipZGklD+3qS7I5JXJ7sWGE597YZ6edPZxF6scckkE3cKoHjFWgfqfH03+vPfRGYlQlVm1Oua4hMGbbIbM3oPZ4Lkxx7CtTR8ET90Nv6ziVRXWNr18T1rRYlstbJKjBXB8zDGpjWRyDnUn7HoEuBwM7jGZyy53VPQKNr8btJeRp+WBYlwbMHkDgAAAAAAAAAGLqm0fW8S8DdUXUwlvQhwefgOJhq+B/m4wAAAAAAAAABAAAAAAAAADDXKMOF3q3Gge2iQcOUc3f5FeqBpUw0VnKwemIqRdpAcAxvX7ONda+DWrym0M8QSuYAAAAAAAAAGG7SIco8TVmodgnBiB8t5KR7U6x/TBrnkQAAAAAAAAABAAAAAAAAACBE4BkFa1JpzFdCs57cUYCokPImMV49Hlx7hNIjOYnQFw==");
        expectedResults.put(
                "4QGNmXBbPHGudvjJpK80YzB6Xj5XYBc84gyGXDfhbDviCjLNseNodDeSryZfMyChKOqDfLGURIUQIuWM34D2Dg==",
                "AAAAAAAAACAFQt5HwnJRaGK64IxT8csDRDmnORhP5wcgjdkoF7LcGgAAAAAAAAF5TeBXDyfK3GYIwZlxnno8D1WJNKcR8mWxIJSSQrivt4K/6bqNqJWBnhtW3ZCbTcH4vDHD4VWn1v/bl2hitFRcQHZhC1pvOUfbvm986En1BxJVef/8wcvxsfr5Kwq3DyOQm2PYWjpgmrEvTMsgmRDHziUG2nx9jn3DxoPnTt5mD1nEOkyLiUO1Y9ATWhqqZhJQlymgoXSkKhwABu4LioFfJJJAe1Bv6allaEHXutsMlCvZGWLd4aDFdQAPC3vZLNPGx3LB/I/JJ52SSFGPlykEzK8Y+/JImwPqNNGmyYLFkbwPjPI/pvbN99WkV55QEKzJXEhmxjij03zCVtgCbr35l8Il3HT0nIBhFxD/yfMYZvKYqPQPi7gud3LsKpcLieHvUrZ0CJOqoOL+bglGQYAGq0E6CIV4E2p8Zsqui4iXJIhC1jwoPwybMuhONi9pnN8nKcPuaWPAOidB9ClD0XYdqClEEYMcgTJvmDJPOX+U2XLVgwAHVCNTlPsAAAAAAAAAGG4Yz6i5S4o9kScKOxa4fUbDzzkyqmtdbQAAAAAAAAABAAAAAAAAADDP3iqlBK1xTxAkN3unzUBFTrIOvm8C1ZiAj3FHqWwvaQIO1R3T94eDiGsvDS0VyPEAAAAAAAAAGBVvBdR2YL2QlyC83KpdjvRbfySUPRgAtwAAAAAAAAABAAAAAAAAACBE4BkFa1JpzFdCs57cUYCokPImMV49Hlx7hNIjOYnQFw==");
        expectedResults.put(
                "KyeKZ8Y3SO/5TQAXouAkqBz8u1nFcZaQ4tTGzcGKwdLYRjwZg69ID0xtxSGy3wn/uAuvktmZinU429ZtQX70kQ==",
                "AAAAAAAAACAFQt5HwnJRaGK64IxT8csDRDmnORhP5wcgjdkoF7LcGgAAAAAAAAF5qBW+xJ5EtXHIxTviRfsm/KKAN3z4vtzdm5ufG6LiFP5PA+ltotXsaeXY/l36Vq83ryFqHbQT0/VoYq1yLumPlJN7i+YJVzge4Iu0kdixMz6nZnc3MzQFZaI66vEpJ4HeIrRS1jPwN/JfEbFS9vpH5Paq8kVpEtQIQeBc26WmHdW9F+Gz6JAGsjf10K5qIiJsAbBgRCfXV3BbuSf1Ew7Hha1LE0gh0rA4+e776eXHbP3zOeuZfo0cteBFy0eH/3eEzoahjJDhRDq4oF7PM13nTI6zqDetEu8lMimOLKGE7HTR9L4mdbIwz4q6jI9g/jyPcGEZOXqB2UHr1uFudEJOZNcIFnnEQTF8izTac7eiWB8yhY7h4zfocjLRy+iaQj+eNjU0B1P6TWhp/p2uKpWCN1j10sZF5/kBTvNy5AwHqNWC11JxPKPz5LhLHaGKEuMpRQuuUDSuOn0LL+StreWjhX04MciOoNQ21Kc4jE9jamdmJmgLzqw9pEsAAAAAAAAAGHxYYnCoys6csq/gFdXfhCygX2gBx6VFVwAAAAAAAAABAAAAAAAAADCUTOj5mklOXfJqARyymeaWfsAVpTAJ/RPWzG6pnmHFDD260EmN4wHZTxLJbkup8DkAAAAAAAAAGDikykkuBRyN3hYGxA3CY88OKuwsLmcm0AAAAAAAAAABAAAAAAAAACBE4BkFa1JpzFdCs57cUYCokPImMV49Hlx7hNIjOYnQFw==");
        expectedResults.put(
                "cX6H8Bup2+9/I5b5MydBYVFkwaXNnXsWNpQqf3GsJhakhRIHO6Wji6eyPpAcfW0qiu30eMWb/GQGiy/XLcTeYQ==",
                "AAAAAAAAACAFQt5HwnJRaGK64IxT8csDRDmnORhP5wcgjdkoF7LcGgAAAAAAAAF5Z6SsdzuX7EjcANd5qQxZon9Uc2CSQt/8LE9pNVBzQuubIOmC+q4BRFXNvmaGQKWqhoUqlF/0wOoSdo53XKtmA49ByAenaBFpa0qHRfUIalTR+NrwsAwazZ/lOq1PU+br98Ni1eszVB5pJ0cbk1k0AmCCaeO11JSFIXydrnjsc9eqxe+9ot+wOiMMbj5yaqt0TNH1hp/C/q6i2SGqHNl/Fx2OsXSc82Z5DvRlPqJvOUtQGFwDTGlm/yfPUY2MM0p5ncPPr4Yf52MwdgyWK9/KKBIMt04p148+DDt9b0Lc//zzn6s/LuB3w1zuQ3dKRkUqv8NBugcf0/HVg6HJjV59XZ2rO3mq301OOT6Ijlgh+3OcrdDkAPzdVs+lssGXbvhGNiyyQASt4xPeP17+OW3XyHjpyexwVCmwCNTwiytgeE+kD5Ub8EsnI5sXQ2zKRAy3ue36BGRO79Bjk/UsC2SDarKZoJbF+AhWykfjKgIMeqHFrWcKWDAz7nwAAAAAAAAAGOeuGEFvqPwGuxqgNacsqFGD+yNFVc/kXgAAAAAAAAABAAAAAAAAADB8fiOdwII3+ycQGQmAd8x8FFLrTrrkOAkJSA0z+CMalcmuIvQ56QBFhfbXo7m2OgIAAAAAAAAAGLuU0s6A5Kjw9x3Q9ZmFif6IRzkB3pkLYAAAAAAAAAABAAAAAAAAACBE4BkFa1JpzFdCs57cUYCokPImMV49Hlx7hNIjOYnQFw==");
        expectedResults.put(
                "cLspV5WrjzWAvjsIyK9nR98YJmRcd3BUquxUQZynvMTIOiNi3TqB02jLUM5y/gBL4cU6IGwWZHgGzeSsqDQopA==",
                "AAAAAAAAACAFQt5HwnJRaGK64IxT8csDRDmnORhP5wcgjdkoF7LcGgAAAAAAAAF5YKVCuBYVnGi8xSltargjlxCHkHNNApY8TWyvNsYeLb/XRAjwCnC2Yt3jbMsM4TVBPvxdOrouCFREmCuRhRqb4/+q+9TRJtXgp1neNS41sKJgKkHD1c5kZi6oGba0wEKEUA4038bL8Ljcsh/i83zmSr3jo3m8JAqXbyFPdRKYeYYZypZ41s9GsjP4Vl9iMAe3lx1TIi23MrOS+Ld27lDxZVdAsO0PjsUQVUMaWCcCPlU2Afb1Cs7Zd5kjCHLqdMFyXR9jvjqbR3Oi0C++AvxHzYUEv9VN6C+uq7lGyeFrzGDSnD+fBC7HWoQmHJ0jrFz3X5VS/OvsrpFUtue9+jPVAzyq8265GClcKK0wDXXFbMUN7pgZj0kQPy+lI4fe1FlwU40ON0nnaMWhPeAi9VVpBc5Eq/L8sTqJ5wDO79GoOGJ5pl36/W+QwfTJbGk3ge8Edb7ombRXCcVid5GJKsxEzLlQeAQmeVy17dUtfRDOiI0N25Np9HefV6wAAAAAAAAAGOb15oKUVPy21hAegEsiXAjTY4zpGEqYnAAAAAAAAAABAAAAAAAAADBR2o8WvLJWAqHNIwzd9bsonjYgnhdt2gaADeG9DqlxLi9MC81E/PB8OYAYw8mgI2sAAAAAAAAAGNk0ShxI7iczo00CM9Lp5ZAKx1M2zQjKoQAAAAAAAAABAAAAAAAAACBE4BkFa1JpzFdCs57cUYCokPImMV49Hlx7hNIjOYnQFw==");
        expectedResults.put(
                "3tCKuvTQvPE9tfk0bSyYUavAQ9dRatWR/1+lKTcK7VclTyX9cnHhkZIzLAgOcqtoBYDfU1PXxfiwp5AOOEptLw==",
                "AAAAAAAAACAFQt5HwnJRaGK64IxT8csDRDmnORhP5wcgjdkoF7LcGgAAAAAAAAF5hrqCmCKYxILoMhFtIIWPo1F6HD1HXmHvyBBOdyqcQtRWA6jZk6Fb4GTTHwYQyRr1C5dleX0wKmfUR6anpQ5sbZuorTW9aDpLzU6jMkmLJr4P34RxiVYL2VcgntUW/a7S1SNgg/2SYiBb8K6nDpVGZ9T5qqCEBuNp8zsMOMOK/Fpf21Mupz2sgFLZZaAdGEEUJFOT6TiNrO6WChdkrfK1dKPhJBCi986WFMmMgtFXhd63QVEUpaBQy7ImmmBDdn4AMjpXXe6wdqIZ15Tt9Tun5EPDL5tcXym7AR/tV2kZCrIXJIN73XQd5tBiOJtIgr2urX2KL3HlqLxR/lgZO31xkf2Df9NNGuVDb3bn5mp10nJNacPpC4B4iOWIrNUiKr7yizwZ0zJVlL50I2tWE9H+fFh5OG5c61LC3deRYfuzr33CfdDgbgr9F891XKYUCbaHlAlsQtFClsUK+qCQ0e0KcEf4rH7sRYXuClG0d8uOYn5Qz0NHNo7TNFoAAAAAAAAAGBDeWxJAwCjCX4HkW7bhhY2Jn/hsqlf6jAAAAAAAAAABAAAAAAAAADDENgHwSduG7naSpi2niNCzOVPWBDi7wmqLaPV9J1SZB9wyoNq8byHaVm+o2TsRFvsAAAAAAAAAGNgvMiqpT2RM/jQSdTWAqaLefxlY/eHUpwAAAAAAAAABAAAAAAAAACBE4BkFa1JpzFdCs57cUYCokPImMV49Hlx7hNIjOYnQFw==");
        expectedResults.put(
                "vBbtn++S+TB8TufA8YuaZNCqxJWqP56CJZ/yvHzBYjKr8Ie3UnrIHeOSThW53PchiWDC3ctdAFyj9UhL+8sIDw==",
                "AAAAAAAAACAFQt5HwnJRaGK64IxT8csDRDmnORhP5wcgjdkoF7LcGgAAAAAAAAF5vLRDFQNI8W2ONuGZhOWc+Ba8qbBebmzt5dzurVk9K6nop1noqViLDwoO+HB6azsnheZ/2T1iIZmQu04TpJYVHKFeoXCYNmn496lGCjdVFvB5UKCMX8RxA/FkBfbm8I24+152VtvBRFSKkrDVY2+hO/5pQAwS+5EBg7Sn1NpuQW2F75jpL1YVIbPmuLPeYAeDNB0Aavc2fzVEeEqYGfFCjJf2s9K8T7lXa+Q32wZcwJnsFyoA/XqJGfbMfar7p3i0MMISYVaMhlFyvfYkpJl8g/JBQJeTGh+6X7ZaDONzB8fv7os+gU276nhDKFt+lwVBNo4i/eTRNp08H+2v06uEt7KHhZ0cH5tfgKQqS0b2Pasp19Zmm9DxE+mboKHr6m8CsjWUQNZ3O9MSnBD1yKq1gcxBO4N2jrwWcOudww3Gw/WHB/yL+3lYRbRDdVvbKNPhDVu1LY4Bv0xCoKHpXdyAEFolRbBJU88Ik9jvd094JcQnUBnr804Zo+IAAAAAAAAAGLVg0NDFX8MOOYOk/xxVzv/HUK1Cj4Z9jQAAAAAAAAABAAAAAAAAADCD9DkhtY9L1nCX2Yg5cfEzfLYK9lle+laU092oZGeU6cDy9kH0DBkgezjIA7WBgGsAAAAAAAAAGHLXfjo0og2bi9fjKc7Q/G0sZPU4JDouygAAAAAAAAABAAAAAAAAACBE4BkFa1JpzFdCs57cUYCokPImMV49Hlx7hNIjOYnQFw==");
        expectedResults.put(
                "MfirpKSm+9ra7oyEgyuPy/Yuadqbqoj0tEWiGQwtHvLJQxS5Mc6Xh8Y6+f8mmq5/OEQa3bfQS3DlSWUAnzB/zQ==",
                "AAAAAAAAACAFQt5HwnJRaGK64IxT8csDRDmnORhP5wcgjdkoF7LcGgAAAAAAAAF5EDGK82TECZ0MmHVFy94aR8M2ojGFf2AzlOosAWZoGoKCc06z5CUuxf+Y7/l3xCv+8Kyj99lgI5ZVjGo/qbKSC890Q/6lz+k18mAWc1FUGZPj2O6Ek5yZ5kL7i5b0uxECbXCf/n2njEk7omAu+Yx311G17TCPgljOLcQDjYsnAcukrHXO/w1A/J1H+puq6YqVF5mURLYPr57QJjV3DUxXhWvxRdQV1DOUqVaedLXnrZwdQgG+K/gXi9eeIUOaOapD3xlrjmVDY4yvouYFApvv7EL6r6gT4ClORxDN+vBOhMTR8guXfA9ofFajJGpBacTL72t6YNv2dTSk64T7pr3ypEy0DRBikRNIhvClv/Iu6+bCO2lJYgpZT7gA34wB9Rmib9yTYTpAgtQaW6+M60oNgIYdScdDIG8ErQotgFvKIhdjiaBfpDCSEB7OLGNsdZblv4mmmWscDdWUnfiqn8QRzJfetQVv2vlQOxWOyAmaJ8qofTRpu2rviVIAAAAAAAAAGI6Knfls7CB0/DXjNjbRUCPBzoxX1ugL/gAAAAAAAAABAAAAAAAAADCtJ4slZdxSmBXiOXhMpv1nAbzK6zqSwn2rJrVTnKNfkWFUr8XVRg5Hd3/+7P6Lv08AAAAAAAAAGFGVexjQfhBbyX9DpmgmWdzMy2sw6piNFQAAAAAAAAABAAAAAAAAACBE4BkFa1JpzFdCs57cUYCokPImMV49Hlx7hNIjOYnQFw==");
        expectedResults.put(
                "ZnMCghWBi/IRnOtyXS1U3iVEfo05NiRcjaZdQO7cGeql892zOvCF87rSvFQzc2U/29Jr3JWds9Vufo7kkF9OLA==",
                "AAAAAAAAACAFQt5HwnJRaGK64IxT8csDRDmnORhP5wcgjdkoF7LcGgAAAAAAAAF5JFFezlYK/dhhJw/xuUwBvilTuqCrrQ/QYO5+gbkvSsGZCE970luaY9DAF327t4qW2Hxn5ngTs7RjhRbMdeuMaa1QrXsxsID5s1Zik5SbGsed9ZfPOq9GZ2Zf2YYdERt4AhZDDnu/9DTRxiz9ly7CvEfl27etatDn0IwwYrdGhh4Qqf8MKsyuOtKByFhUNXBuHf8fG1168sjXiKyVXlOlUXIUVjGw4+QCrXAT9+fzL9dcVI+5/oM5ZH8O2Lg/kaJZ6N9DK6F2NETDkmRXcjUCveSV2ZUqHM72Px3/BVtjS/TmmWNNpB8pd7ZNCGzuqzWtiRXBYOyBco3axgqz+HisQwoogtQFXtXZbiAty9KRGWJMO190GQO0UtZbSlFzNgNPXrVyBoX94mLjRg+ehwQUbkkz0RfXXa13ZJ4iKDvgXcFn3JprFCeGRCHH9VPydqgpcdtZbzBHKSDNeBHhs5UbhLNaMGNiw4cNFMgVV3P8e+jt8I/NvaXSx/gAAAAAAAAAGPvTCMu0fpZmDFsjr35LAFuIAtWTrHXrmAAAAAAAAAABAAAAAAAAADCg5XGUSbsvt3iTlJ6sXjGgs156MGYBRixFXsJ9ri3mrfgBwIvgeinVHvuM62NjpW0AAAAAAAAAGE8UTbdtd/6sTNpAxpVmCXA01enEI/QuVQAAAAAAAAABAAAAAAAAACBE4BkFa1JpzFdCs57cUYCokPImMV49Hlx7hNIjOYnQFw==");
        expectedResults.put(
                "WUR8b0agb0opr8RORuZRmILWFVASfJsl8+nUfWGzlHSdshdA+nNXZz9OoJ6hJK0ZQh3C9q6GC5w9Zhf/jhsKjg==",
                "AAAAAAAAACAFQt5HwnJRaGK64IxT8csDRDmnORhP5wcgjdkoF7LcGgAAAAAAAAF5GQDhBq/tMwKypFngAbe05A81mGqZ3DuRWEUVZteeJDVU4p7VXSub6E50RsXMVasf/H4TTrcTcSc1TOUF7LRpKez9gO3AYaxPnY5e+FcW98/dEmAFhXbdnpi9H51nz/kljzjACfjU5k1DKwVD6SOqjRqjBqzuQaC85XXQwdjA6ehUlMbBigUpP7BD3DyR31DH8h1sIp3c/fTuCrb4xyqJxc6FH+N8NKpKl5/EFdMAiSmkCuy9P64CRMBfJ1hImr2Ri9CcEtWw4UYQES96/6QmYq0+qOe/VF2kceXmuz3/Sui8pB02O/XaKzSk1g48hA7zzK/gPtnCyZw17CxXgbvTaPI+YcSIqtiU7He3rOGlmZu/ues+YvzcUT9Xfno7SS4fepSNmSdQcWH81ypxQXlXYRqt3jjno6dLb0CoJgAn4rnanRT48f+nQjuKucRAF0UpY9EOlcSk5u5w5+puCaA0Oi9mTuSQbUjaXoKxPCSWd2IHgNt+TG3GEdoAAAAAAAAAGEDWpV1W6/QzKoOdDQhooZuTMAa/ULs1TwAAAAAAAAABAAAAAAAAADDVIQPyHaosA3QQeexG6Vg2tl7BloNZVH5DKyEqLP0108pR69D5YlEm3FPh8FMwf2IAAAAAAAAAGHji8FjZIzU339/uUu13Ec3pmlCksVLm3gAAAAAAAAABAAAAAAAAACBE4BkFa1JpzFdCs57cUYCokPImMV49Hlx7hNIjOYnQFw==");
        expectedResults.put(
                "YNa5elbAABV+L+ilFKZ4l6L14n8ka8cJDAh+qOJh70ffYqTn0F5a5obeCgMah/kAPLQ94SjDWYCRLDJnv8FEdw==",
                "AAAAAAAAACAFQt5HwnJRaGK64IxT8csDRDmnORhP5wcgjdkoF7LcGgAAAAAAAAF5Q119COgjzj/ldvFKuokGIOmC06X2Edce0hK+h8PvKx5VLXNrNBr/lbZpnBewV6G8TU4xgzxm9dx13iL6N8RQUJPI/O54QN3NYxMTNlpZXe9YM4joXJXhCK0omfFwbunVSk03k4NUrj1aWVncjtzbsDYJ+D4Hj+nHdxLeC0y1UTCjQCnHJnNiaEvwyHsRu4XeJvZuWl+9ww06IQsYvhNjbjEAKG4Eq3VnbuK/EVRoFcwq850QQL9k0YsbNubGRUKljrasxk5jYWo5hxIKPjQ12jquNoX86XGAF9aTghw9Zguyi1s9lR61Zt3eIcY0TrJSZP0BVcsplYVpoyJ7e2ofQspJrR2gmR3YN6A60y2vKl5By7rV/6BJmyskGDh6Zya4Ussa0Pbu9vJUq/ZVMxRlqB6zczEUC2ad6zLojFbKBN9PVs8uXZ9fWKfVZiCYSPHLB58YyfyuyeZDIfDoYVZ8AIvCKeYIREsoHz8wWqfFrA99vR8y52g0W+MAAAAAAAAAGEnxS2mMT4iyGE25Bat6YDLSL22lOH/WjgAAAAAAAAABAAAAAAAAADBAHxKT01jrxc8er6fmmIoxrz90sRGoqqpuqhpvJG0Ka9bvpCBGziz2Z096wu+/4/MAAAAAAAAAGFEILGT8bDqgv2+LEW1EkLBy553aqA6PqAAAAAAAAAABAAAAAAAAACBE4BkFa1JpzFdCs57cUYCokPImMV49Hlx7hNIjOYnQFw==");
        expectedResults.put(
                "qFwn0kCVzekKgtsLpSek2JRobDT69y/C32fFcxEplAqHKDmkg4MUThp8BQFL9Ieqkwcya5yqZJXR8fU1yOTVmg==",
                "AAAAAAAAACAFQt5HwnJRaGK64IxT8csDRDmnORhP5wcgjdkoF7LcGgAAAAAAAAF5bZYEYMFLmdH5OrBjcIbP1lmpGtbBU99rQE+KZoaBSlLuaMHVWPYqljSNChl3EBr6yNZ4vw0oKJ0noCHCc8zPFaBm93MkvnyukU6TE7cVikzkldb7KrTyZizSxziHXyzgFWbaLE1R2LPd5cCjQd4DkmpK51bSkA4BJYQBKQWrNICt9vKJ3+0Hz/7p5cOyDOGO4Wfp2T47g+DaLkr2cGMZN8XiMgl5N/f5QLLD01CXJWQEQ5HuSJ6y/PtJN8mxUD87Bw0onswFC6NDvdhjJ4Co8e7jl/Cf/SBDYCN+oggMCpIPzi9XonelFV2gbJKgVF2lsfcWNGhR5vgimlEQd7AsHEXDZpB8L6Tn6Sr3Oimsjs6DvxEb5cMjybeSHq2t9hhsrFga8OjkmEqABiWZ5ZIw6kUHQ5l7ke1Vt8GETQq8P/7IkUbbCjDD9fER2EghjdWFH6Csm/z9oJAmDtxm4xa3v47ydiX3WdOVE0cmCwWAb/nhnIR+lvhJxBsAAAAAAAAAGM7QQf/HNRdyXOLlaRLUHPsXQkbFl5/CTQAAAAAAAAABAAAAAAAAADCn+SBY5zmqWn+RJA4Sq2fvmJv0POTtK3vWD/q0vMTc6NFbsrz5/RbbvES3p7msZh4AAAAAAAAAGCowOFcyIzcy99A6s4RC1ReYWl/Wcb4RSwAAAAAAAAABAAAAAAAAACBE4BkFa1JpzFdCs57cUYCokPImMV49Hlx7hNIjOYnQFw==");
        expectedResults.put(
                "vMRe3B46AmgM/dTgV3HwknSqNekRC69mDEyjg7AnD/p/cWejqumVRT5/Shg0p3BZB2ZvamCTgmqXv/3ojBoo6w==",
                "AAAAAAAAACAFQt5HwnJRaGK64IxT8csDRDmnORhP5wcgjdkoF7LcGgAAAAAAAAF5H8tHdXjhj/KelI1mT3gBJH23qR9XSfc1hl9Sdp5t9M4N6I378LCoQ4hyCXCay1JDYQnHLzmWM5/BjQAVxA9/XyM7CpV+Be6TOnBBybBvABfDGWgiHiYjjiCYAuODLNY18Dc6TI8U5D0Knnf4QyNf7HroX2U4SCk+b/jTNM2OUcWtk7SKQfju07ftOZyGCyxgpUg4jI+nT52f55QRg0AxG7v5UpCAQeHZGnzHqBT7MmxkTvXjOXMHG5+cfXb9JsD3aTFtJRU7nYfiEGbvzhlqI/EyC30138EPXbu37omhv3QLf3o6N7i0imLfvJOmHTnZN7+eP7MxqV6yvljuwnDU74xTgICw4m29x4EfdkgH+KU8GsGofhdDvkWt9Fl/xqa924kcW+SlK588ZUFBaceU8x0JPX9pFtpTXrTFUT6SnlJ7bcJ11v75HjnUwOw3D3eMvwXWkrNTKK6Plc0gFteqI2Cxs+e1QAfUwpHgarp71mu3Z/iuFjy6kQ0AAAAAAAAAGJlY9LmOcer89dXFm9SaPExa+jjIoq9/pgAAAAAAAAABAAAAAAAAADAuIe6BZXSJIqVPhK3B7Hiyg6sAA9DtkwKq9PbofJXwrZ6niKeXW/lM7A3Z2RkGi00AAAAAAAAAGIYh5XMdIHWw+FS6YfutyA9aooeh4H/5EQAAAAAAAAABAAAAAAAAACBE4BkFa1JpzFdCs57cUYCokPImMV49Hlx7hNIjOYnQFw==");
        expectedResults.put(
                "AjBtJ/t+bcyZzg6w9jya8cVAR2RHrTE8dh4xuTwQJtekjEMuLLqz9s6GzguKvhpX2OrJ5m6Ey6b0sbisDkZJYA==",
                "AAAAAAAAACAFQt5HwnJRaGK64IxT8csDRDmnORhP5wcgjdkoF7LcGgAAAAAAAAF5feZGTwzQL3cUYbEIR3YBZFCG+M5PPkEjxLoo6GzKO2XT8hjlzEgxmvoRidJMiKkky0rMercnLGqNINeWb1oX3LX42+vR5nRW5JgBlsSdox3ORoKdUGSdNX04U+uIvUtIVbcl6xVgzbNSIqkiVtnorqWktR24J+TCM5pXdT816fpC+PGtqQwl0hXpBeViXAf50ZjB2zIAZDYZG8mEd+cmGnoE5akQzOViEv8KWEY13S3+NWoPe9OKwGzvbX7s2lUXd2SMsJ5jT0JwEo3+B3Y8DDqc5QPr4DxuqQASbdTmy7LcDX7DPVa5Ur6Zl/9P+hNT53Hy5thTd+v6MxPVjIF0PLi7vITKPzufldnXoVQwkH4rDJu6T7BjRhfe1sE6YfGP3YCn8DJLVkzqklcOhPkMtfzKnMkZ2OyY9JmbBs/tOU14vOtpHwyY0cGelKwxX2IldwxZnYqExkmSsyE5M7SkNnKbkKvRREkP0jRKtgGzEx4rK8mPUZzixWEAAAAAAAAAGA1Opltvc52/yTFw4xmwy0uErUXiEwWnbQAAAAAAAAABAAAAAAAAADAniKfUZR2wVsPqATo8C0j7jNXQSIjJHMLJgKvu+uv4E5iO5o2iwL8H57ZhJ9J1k+sAAAAAAAAAGJPJSsjGhrZD3/mLoP2ywiy3zQJMa2uuXwAAAAAAAAABAAAAAAAAACBE4BkFa1JpzFdCs57cUYCokPImMV49Hlx7hNIjOYnQFw==");

        this.loader.load(directory);
        final Map<String, String> results = new HashMap<>();

        DataEntry next;
        while ((next = this.loader.nextEntry()) != null) {
            results.put(
                    Base64.getEncoder().encodeToString(next.getKey()),
                    Base64.getEncoder().encodeToString(IOUtils.toByteArray(next.getValue())));
        }

        assertThat(results).hasSize(22).containsAllEntriesOf(expectedResults);
    }

    @Test
    public void loadLargeFile() throws Exception {
        final Path baseDir = Paths.get(getClass().getResource("/").toURI());

        final Path directory = baseDir.resolve(UUID.randomUUID().toString());

        Files.createDirectories(directory);

        final Path largeFile = Paths.get(directory.toAbsolutePath().toString(), "loadLarge");

        final Random random = new Random();
        byte[] data = new byte[33554432];
        random.nextBytes(data);
        Files.write(largeFile, data);

        final DirectoryStoreFile directoryStoreFile = new DirectoryStoreFile();
        directoryStoreFile.load(directory);

        final DataEntry nextEntry = directoryStoreFile.nextEntry();
        assertThat(nextEntry).isNotNull();
    }
}
