#



if ( __name__ == "__main__" ):
    import vlabif
    config = vlabif.DebugConfig(dc_name="Autogen_Debug_Entity",
                chain_config_file="chain_config.py")
    config.writeVHDL()


