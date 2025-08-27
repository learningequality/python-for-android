from os.path import join

from pythonforandroid.recipe import PythonRecipe
from pythonforandroid.toolchain import current_directory
from pythonforandroid.util import rmdir


class ZopeInterfaceRecipe(PythonRecipe):
    call_hostpython_via_targetpython = False
    name = 'zope_interface'
    version = '4.1.3'
    url = 'https://pypi.python.org/packages/source/z/zope.interface/zope.interface-{version}.tar.gz'
    site_packages_name = 'zope.interface'
    depends = ['setuptools']
    patches = ['no_tests.patch']

    def prebuild_arch(self, arch):
        super().prebuild_arch(arch)
        with current_directory(self.get_build_dir(arch.arch)):
            rmdir('src/zope/interface/tests')
            rmdir('src/zope/interface/common/tests')


recipe = ZopeInterfaceRecipe()
