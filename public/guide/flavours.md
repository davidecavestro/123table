[Home](/123table)

# Image flavours

_123table_ is packaged into flavours for specific needs

| Flavour       | Suffix      | Pre-warmed | Startup | JDBC drivers | Weight |
| ------------- | ----------- |:----------:| ------- | ------------ | ------ |
| Generic       |             | No         | Slow    | Included     | Heavy  |
| Slim          | `slim`      | No         | Slow    | -            | Light  |
| Fast          | `fast`      | Yes        | Fast    | Included     | Heavy  |
| Fast and slim | `fast-slim` | Yes        | Fast    | -            | Light  |


## _Fast_ images

The *fast* flavours leverage features from the https://crac.org project
in order to speed-up the container boot.

This is useful because to boot _123table_ a JVM starts
and scans the classpath for all the libraries plus the JDBC drivers.
Since this can take some seconds, it easily becomes cumbersome.


### CRaC insights

So CRaC is employed at image build time: _123table_ is started passing
the env var `CRAC=save` so that a snapshot of the JVM memory called 
checkpoint. This checkpoint contains the tool code along with its
dependecnies among other things and is persisted into the `/app/cr` folder.

From then on, starting _123table_ with the env var `CRAC_MODE=restore`
will restore the JVM state, and the restored process will
**readi args from STDIN** since there's no way to change the original ones.
The trick of piping actual args to the restored JVM using STDIN is done by
[the entrypoint script](https://github.com/davidecavestro/123table/blob/main/123t).

Please note that there are some potential portability issues with using CRaC
in this way, as the features of the end user's CPU will typically differ
from the CPU used to build the image.
That's why the fast image is built using generic CPU features.
Also, the images that aren't tagged as _fast_ don't use CRaC.

For special needs, you can also save the snapshot for your own
environment/CPU by passing the env var `CRAC=save` as mentioned above and
possibly setting the env var `CRAC_PATH` to customise the path or mount
a volume to its default `/app/cr` path.


## _Slim_ images

The *slim* flavours avoid packaging JDBC drivers in order to reduce the
image size and avoid headaches due to conflicts on JDBC drivers version.

So while the generic image packages some drivers at path `/drivers`, the
_slim_ ones have no drivers. It is up to the final user binding a folder
with JDBC drivers at that path.