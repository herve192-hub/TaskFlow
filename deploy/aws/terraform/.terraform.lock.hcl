# This file is maintained automatically by "terraform init".
# Manual edits may be lost in future updates.

# Project context for this generated provider dependency lock:
# versions.tf permits AWS provider 6.x; this file selects the exact installed
# version and package checksums. Ordinary init reuses this selection, and the
# Terraform CI job uses -lockfile=readonly to reject an unexpected lock change.
# Commit it so developers and CI resolve the same provider. Deliberate upgrades
# use terraform init -upgrade followed by reviewing this diff and the plan.
# The lock covers providers, not Terraform CLI versions, AMIs, Docker images,
# application dependencies, or remote-module source versions.
# Checksums are verification data, not credentials. Do not edit individual values
# to bypass a mismatch; investigate the package/platform and regenerate with the
# Terraform tooling. Future init operations may rewrite these explanatory comments.

# Registry package source corresponding to required_providers.aws in versions.tf.
# The version is the selected release; constraints records the allowed range.
# h1/zh entries use Terraform-supported package checksum schemes and may cover
# different platform packages. They are not AWS account IDs or encryption keys.
provider "registry.terraform.io/hashicorp/aws" {
  version     = "6.70.0"
  constraints = "~> 6.0"
  hashes = [
    "h1:HCr4MYgxlkFBOAeWKOoktNNzZaQX8ZZY4YTxmL/1V/Y=",
    "zh:068ca7ed4fc77e3d63d7fac54515a426a32f3dc143b55a6ad00963fe68289955",
    "zh:2d8c016b3809ab32f536b01bf2c98fcbdeb066b7b7e50f756ff6ff1e0b0539c7",
    "zh:450d960b60ea81268bd355600340787254f750d76e0075208a6fb903d7336b09",
    "zh:50447833cce21288ffc331342f9d83854e80515e2d86e8f5776a07ce18866b20",
    "zh:58acc041b080778ded75320e241ebcf6cf2ed145bede644b86a4784ce46086c6",
    "zh:7e899a44a1da95fefb1b04b231cf7cdb5871f482f60a710ede78bf25ac265724",
    "zh:8b141d13f4b84d7a47bc5a4855d0fc985191317b3249603532d4de03db5741c5",
    "zh:9035357f7d065ce8d35a9b05337977910683219c0f13fa09155d28f9b32e23a7",
    "zh:9b12af85486a96aedd8d7984b0ff811a4b42e3d88dad1a3fb4c0b580d04fa425",
    "zh:9ba0990a876d2641a185ca88263719afc4d5ac43533bdb147437f0073d2cd74d",
    "zh:a8773c7a02e1b3290df89351ee493fd8b74f807f92a7f69534e05901d4515761",
    "zh:ab0ebb298578eeeffe0fbf0a8b27ce99acd4419b7049272f1ab87e8c47229407",
    "zh:b88a3f22127bfb17604adc53e9bdb8ed0eddf4b9b77bf19871cba2e7fcd5b4a3",
    "zh:c7b1fb41e7f086b670f78932ede2b539903a8d71bac06f55f14a1a33d09a1ccf",
    "zh:df482bd93f51125f78feab7742aae1d7f22c7108ee2dc47787c4a1d611e6ccfd",
    "zh:fca401ffaa42d5f350d667123569de51a23de8c30b87ce0bd9d86fc80835fd4d",
  ]
}
